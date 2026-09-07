"""Local HTTP regression tests: preview responses must reference loadable images."""

from http.server import ThreadingHTTPServer
import json
from pathlib import Path
import threading
import unittest
from urllib.error import HTTPError
from urllib.request import urlopen

from preview_server import ARTWORK, PreviewHandler, contract_fixture, preview_artwork


def image_paths(payload):
    if isinstance(payload, list):
        for item in payload:
            yield from image_paths(item)
    elif isinstance(payload, dict):
        for key, value in payload.items():
            if key in {"poster_path", "backdrop_path", "profile_path", "still_path", "logo_path", "avatar_path", "file_path"} and value:
                yield value
            else:
                yield from image_paths(value)


class QuietHandler(PreviewHandler):
    def log_message(self, *args):
        pass


class PreviewImagesTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.server = ThreadingHTTPServer(("127.0.0.1", 0), QuietHandler)
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()
        cls.origin = f"http://127.0.0.1:{cls.server.server_port}"

    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()
        cls.server.server_close()
        cls.thread.join(timeout=5)

    def get_json(self, path):
        with urlopen(self.origin + path, timeout=5) as response:
            self.assertEqual(response.status, 200)
            return json.load(response)

    def test_home_and_deep_resources_reference_loadable_images(self):
        paths = set()
        for route in (
            "/v1/home", "/v1/home?media_type=tv", "/v1/titles/movie/550",
            "/v1/discover/movie", "/v1/search?query=club",
            "/v2/home", "/v2/home?media_type=tv", "/v2/titles/movie/550", "/v2/titles/tv/1396", "/v2/discover/tv",
            "/v2/trending/all/week", "/v2/search?query=club",
        ):
            with self.subTest(route=route):
                referenced = set(image_paths(self.get_json(route)))
                self.assertTrue(referenced)
                paths.update(referenced)
        self.assertGreaterEqual(len(paths), 9)
        for version in ("v1", "v2"):
            config = self.get_json(f"/{version}/configuration")
            config = config["images"] if version == "v2" else config
            self.assertEqual(config["secure_base_url"], self.origin + "/artwork/")
            sizes = set(config["poster_sizes"] + config["backdrop_sizes"] + config["profile_sizes"])
            self.assertTrue(sizes)
            for size in sizes:
                for path in paths:
                    with self.subTest(version=version, size=size, path=path):
                        with urlopen(config["secure_base_url"] + size + path, timeout=5) as response:
                            self.assertEqual(response.status, 200)
                            self.assertEqual(response.headers.get_content_type(), "image/png")
                            body = response.read()
                            self.assertEqual(body[:8], b"\x89PNG\r\n\x1a\n")
                            self.assertEqual(len(body), int(response.headers["Content-Length"]))

    def test_fixture_mapping_preserves_null_unknown_fields_and_original(self):
        original = contract_fixture("title-detail")
        mapped = preview_artwork(original)
        self.assertEqual(original, contract_fixture("title-detail"))
        self.assertIsNone(mapped["poster_path"])
        self.assertTrue(mapped["alternative_titles"][0]["future_optional_field"])
        for path in image_paths(mapped):
            self.assertTrue((ARTWORK / Path(path).name).is_file(), path)

    def test_missing_artwork_is_404_and_accounts_remain_unauthorized(self):
        for route, status in (("/artwork/original/missing.png", 404), ("/v2/account/profile", 401)):
            with self.subTest(route=route):
                with self.assertRaises(HTTPError) as failure:
                    urlopen(self.origin + route, timeout=5)
                self.assertEqual(failure.exception.code, status)
                failure.exception.close()


if __name__ == "__main__":
    unittest.main()
