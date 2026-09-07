#!/usr/bin/env python3
"""Serve the production Web/Wasm bundle with deterministic v1/v2 preview data."""

from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import parse_qs, urlparse
import json
import os


ROOT = Path(__file__).resolve().parents[1] / "composeApp" / "build" / "dist" / "wasmJs" / "productionExecutable"
CONTRACT = Path(__file__).resolve().parents[2] / "catalog-contract" / "v2" / "fixtures"
ARTWORK = Path(__file__).resolve().parent / "preview_artwork"


def contract_fixture(name: str) -> dict:
    with (CONTRACT / f"{name}.json").open(encoding="utf-8") as fixture:
        return json.load(fixture)


def title(identifier: int, name: str, media_type: str = "movie", rating: float = 8.0, year: str = "2024") -> dict:
    return {
        "id": identifier,
        "media_type": media_type,
        "title": name,
        "original_title": name,
        "overview": "A vivid story about memory, identity, and the movies that stay with us.",
        "poster_path": f"/poster-{identifier}.png",
        "backdrop_path": "/cinematic-atlas.png",
        "release_date": f"{year}-01-01",
        "vote_average": rating,
        "genre_ids": [18],
    }


TITLES = [
    title(550, "Fight Club", rating=8.4, year="1999"),
    title(680, "Pulp Fiction", rating=8.5, year="1994"),
    title(155, "The Dark Knight", rating=8.5, year="2008"),
    title(13, "Forrest Gump", rating=8.5, year="1994"),
    title(1396, "Breaking Bad", "tv", 8.9, "2008"),
    title(100088, "The Last of Us", "tv", 8.6, "2023"),
    title(27205, "Inception", rating=8.4, year="2010"),
    title(238, "The Godfather", rating=8.7, year="1972"),
]


def unique_titles(items: list[dict]) -> list[dict]:
    return list({f'{item["media_type"]}:{item["id"]}': item for item in items}.values())


def preview_artwork(payload):
    """Remap fixture-only paths without changing canonical contract fixtures.

    These are generated demo illustrations, never production movie posters.
    Keep missing artwork missing so the UI can still exercise its empty state.
    """
    if isinstance(payload, list):
        return [preview_artwork(item) for item in payload]
    if not isinstance(payload, dict):
        return payload
    result = {}
    for key, value in payload.items():
        if key in {"poster_path", "backdrop_path", "profile_path", "still_path", "logo_path", "avatar_path", "file_path"} and value:
            if (ARTWORK / Path(value).name).is_file():
                result[key] = value
            elif key in {"backdrop_path", "still_path", "logo_path"} or payload.get("kind") in {"backdrop", "logo"}:
                result[key] = "/cinematic-atlas.png"
            else:
                result[key] = "/poster-550.png"
        else:
            result[key] = preview_artwork(value)
    return result


class PreviewHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(ROOT), **kwargs)

    def end_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Cache-Control", "no-store")
        super().end_headers()

    def do_GET(self):
        parsed = urlparse(self.path)
        if parsed.path.startswith("/artwork/"):
            self.send_artwork(parsed.path)
            return
        if parsed.path.startswith("/v1/"):
            self.send_catalog(parsed.path, parse_qs(parsed.query))
            return
        if parsed.path.startswith("/v2/"):
            self.send_catalog_v2(parsed.path, parse_qs(parsed.query))
            return
        super().do_GET()

    def send_catalog(self, path: str, query: dict):
        media_type = query.get("media_type", ["movie"])[0]
        filtered = [item for item in TITLES if item["media_type"] == media_type]
        if path == "/v1/configuration":
            payload = {
                "secure_base_url": self.artwork_base_url(),
                "poster_sizes": ["w342", "w500", "original"],
                "backdrop_sizes": ["w780", "w1280", "original"],
                "profile_sizes": ["w185", "h632", "original"],
            }
        elif path == "/v1/home":
            payload = {
                "media_type": media_type,
                "hero": filtered[0] if filtered else TITLES[0],
                "sections": [
                    {"id": "trending", "title": "Trending now", "items": unique_titles(filtered + TITLES[:4])},
                    {"id": "top-rated", "title": "Modern classics", "items": list(reversed(TITLES))},
                ],
            }
        elif path.startswith("/v1/genres/"):
            payload = {"genres": [{"id": 18, "name": "Drama"}, {"id": 28, "name": "Action"}, {"id": 53, "name": "Thriller"}]}
        elif path.startswith("/v1/discover/"):
            route_type = path.rsplit("/", 1)[-1]
            results = [item for item in TITLES if item["media_type"] == route_type]
            payload = {"page": 1, "total_pages": 1, "results": unique_titles(results + TITLES)}
        elif path == "/v1/search":
            term = query.get("query", [""])[0].lower()
            results = [item for item in TITLES if term in item["title"].lower()]
            payload = {"page": 1, "total_pages": 1, "results": results}
        elif path.startswith("/v1/titles/"):
            identifier = int(path.rsplit("/", 1)[-1])
            summary = next((item for item in TITLES if item["id"] == identifier), TITLES[0])
            payload = {
                **summary,
                "genres": [{"id": 18, "name": "Drama"}, {"id": 53, "name": "Thriller"}],
                "runtime_minutes": 139,
                "status": "Released",
                "cast": [
                    {"id": 1, "name": "Alex Morgan", "character": "The Narrator", "profile_path": "/poster-155.png"},
                    {"id": 2, "name": "Jamie Lee", "character": "Mara", "profile_path": "/poster-680.png"},
                ],
                "videos": [{"id": "trailer", "key": "SUXWAEX2jlg", "name": "Official Trailer", "site": "YouTube", "type": "Trailer", "official": True, "language": "en"}],
                "similar": TITLES[1:6],
            }
        else:
            self.send_error(404)
            return
        encoded = json.dumps(payload).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(encoded)))
        self.end_headers()
        self.wfile.write(encoded)

    def send_catalog_v2(self, path: str, query: dict):
        media_type = query.get("media_type", ["movie"])[0]
        if path == "/v2/capabilities":
            payload = contract_fixture("capabilities")
        elif path == "/v2/home":
            filtered = [item for item in TITLES if item["media_type"] == media_type]
            payload = {
                "media_type": media_type,
                "hero": filtered[0] if filtered else TITLES[0],
                "sections": [
                    {"id": "trending", "title": "Trending now", "items": unique_titles(filtered + TITLES[:4])},
                    {"id": "top-rated", "title": "Modern classics", "items": list(reversed(TITLES))},
                ],
            }
        elif path == "/v2/configuration":
            payload = contract_fixture("configuration")
            payload["images"] = {
                **payload["images"],
                "secure_base_url": self.artwork_base_url(),
                "poster_sizes": ["w342", "w500", "original"],
                "backdrop_sizes": ["w780", "w1280", "original"],
                "profile_sizes": ["w185", "h632", "original"],
            }
        elif path.startswith("/v2/discover/"):
            route_type = path.rsplit("/", 1)[-1]
            results = [item for item in TITLES if item["media_type"] == route_type]
            payload = {"page": 1, "total_pages": 1, "results": unique_titles(results + TITLES)}
        elif path.startswith("/v2/trending/"):
            kind = path.split("/")[3]
            results = TITLES if kind == "all" else [item for item in TITLES if item["media_type"] == kind]
            payload = {
                "page": 1,
                "total_pages": 1,
                "results": [{"entity_kind": item["media_type"], **item} for item in results],
            }
        elif path == "/v2/search":
            term = query.get("query", [""])[0].lower()
            scope = query.get("scope", ["all"])[0]
            if scope in {"movie", "tv"}:
                results = [item for item in TITLES if item["media_type"] == scope and term in item["title"].lower()]
            else:
                entity_results = contract_fixture("entities")["results"]
                results = [
                    item for item in entity_results
                    if term in (item.get("title") or item.get("name") or "").lower()
                    and (scope == "all" or item.get("entity_kind") == scope)
                ]
                if scope == "all":
                    results = [*[
                        item for item in TITLES if term in item["title"].lower()
                    ], *results]
            payload = {
                "page": 1,
                "total_pages": 1,
                "results": [
                    {"entity_kind": item["media_type"], **item} if "media_type" in item else item
                    for item in results
                ],
            }
        elif path.startswith("/v2/entities/"):
            kind = path.split("/")[3]
            if kind == "person":
                payload = contract_fixture("person")
            elif kind == "collection":
                payload = contract_fixture("collection")
            else:
                summaries = contract_fixture("entities")["results"]
                payload = next((item for item in summaries if item.get("entity_kind") == kind), summaries[0])
        elif path.startswith("/v2/tv/") and "/seasons/" in path:
            parts = path.split("/")
            payload = contract_fixture("episode" if len(parts) >= 8 else "season")
        elif path.startswith("/v2/credits/"):
            payload = contract_fixture("credit-detail")
        elif path.startswith("/v2/titles/"):
            parts = path.split("/")
            route_type = parts[3]
            if len(parts) >= 6:
                resource = parts[5].replace("-", "_")
                deep = contract_fixture("title-detail")
                payload = deep.get(resource, deep)
                self.send_json(payload)
                return
            identifier = int(parts[4])
            summary = next((item for item in TITLES if item["id"] == identifier), TITLES[0])
            deep = contract_fixture("title-detail")
            payload = {
                **deep,
                **summary,
                "entity_kind": route_type,
                "media_type": route_type,
                "tagline": "Every story has another layer.",
            }
        elif path == "/v2/account/profile":
            self.send_json(
                {"error": {"code": "unauthorized", "message": "Preview sessions are disabled.", "request_id": "preview"}},
                status=401,
            )
            return
        else:
            self.send_error(404)
            return
        self.send_json(payload)

    def artwork_base_url(self) -> str:
        host = self.headers.get("Host", f"127.0.0.1:{self.server.server_port}")
        return f"http://{host}/artwork/"

    def send_artwork(self, path: str):
        filename = Path(path).name
        if not filename.endswith(".png"):
            self.send_error(404)
            return
        artwork = ARTWORK / filename
        if not artwork.is_file():
            self.send_error(404)
            return
        encoded = artwork.read_bytes()
        self.send_response(200)
        self.send_header("Content-Type", "image/png")
        self.send_header("Content-Length", str(len(encoded)))
        self.end_headers()
        self.wfile.write(encoded)

    def send_json(self, payload: dict, status: int = 200):
        encoded = json.dumps(preview_artwork(payload)).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(encoded)))
        self.end_headers()
        self.wfile.write(encoded)


if __name__ == "__main__":
    port = int(os.environ.get("SMARTMOVIE_PREVIEW_PORT", "8099"))
    print(f"Serving SmartMovie preview at http://127.0.0.1:{port}")
    ThreadingHTTPServer(("127.0.0.1", port), PreviewHandler).serve_forever()
