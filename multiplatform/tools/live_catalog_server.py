"""Serve the Web build with unmodified catalog responses from a local Worker."""

import argparse
import json
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.parse import urlsplit, unquote
from urllib.request import HTTPRedirectHandler, ProxyHandler, Request, build_opener

ROOT = Path(__file__).resolve().parents[1] / "composeApp/build/dist/wasmJs/productionExecutable"


class NoRedirects(HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


class LiveCatalogHandler(SimpleHTTPRequestHandler):
    worker_origin = "http://127.0.0.1:8787"
    upstream = build_opener(ProxyHandler({}), NoRedirects())

    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(ROOT), **kwargs)

    def log_message(self, *args):
        # Search queries and future auth parameters must not enter console logs.
        pass

    def end_headers(self):
        self.send_header("Cache-Control", "no-store")
        super().end_headers()

    def do_GET(self):
        request_path = urlsplit(self.path).path
        if not request_path.startswith(("/v1/", "/v2/")):
            return super().do_GET()
        segments = unquote(request_path).split("/")
        if any(item in {"auth", "account", ".", ".."} for item in segments) or "\\" in request_path:
            return self.problem(403, "catalog_only", "This local server supports public catalog requests only.")
        request = Request(self.worker_origin + self.path, headers={"Accept": "application/json"})
        try:
            response = self.upstream.open(request, timeout=20)
        except HTTPError as error:
            response = error
        except (URLError, TimeoutError, OSError):
            return self.problem(502, "worker_unavailable", "Start the local Worker with its TMDb token configured.")
        with response:
            body = response.read()
            self.send_response(response.code)
            self.send_header("Content-Type", response.headers.get("Content-Type", "application/json"))
            if response.headers.get("Retry-After"):
                self.send_header("Retry-After", response.headers["Retry-After"])
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)

    def problem(self, status, code, message):
        body = json.dumps({"error": {"code": code, "message": message}}).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--port", type=int, default=8100)
    parser.add_argument("--worker-port", type=int, default=8787)
    args = parser.parse_args()
    if not ROOT.joinpath("index.html").is_file():
        parser.error("Build the Wasm production distribution first.")
    LiveCatalogHandler.worker_origin = f"http://127.0.0.1:{args.worker_port}"
    print(f"Live catalog: http://127.0.0.1:{args.port}/?preview=1", flush=True)
    print("Catalog data comes from the local Worker; no demo artwork substitution.", flush=True)
    ThreadingHTTPServer(("127.0.0.1", args.port), LiveCatalogHandler).serve_forever()
