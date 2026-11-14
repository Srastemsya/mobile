#!/usr/bin/env python3
"""Loopback-only HTTP fixture for FE-F00 profile-incomplete UI verification."""
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path

mode_file = Path("/private/tmp/fe-f00-profile-mode")

class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path.split('?')[0] == '/v1/me':
            mode = mode_file.read_text().strip() if mode_file.exists() else "complete"
            if mode == "complete":
                self.send_response(200)
                body = b'{"profileComplete":true}'
            elif mode == "incomplete":
                self.send_response(200)
                body = b'{"profileComplete":false}'
            else:
                self.send_response(403)
                body = b'{"error":{"code":"PROFILE_INCOMPLETE","message":"Profile incomplete"}}'
        else:
            self.send_response(200)
            body = b'{}'
        self.send_header('Content-Type', 'application/json')
        self.send_header('Content-Length', str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, format, *args):
        pass

HTTPServer(('127.0.0.1', 18080), Handler).serve_forever()
