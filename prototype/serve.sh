#!/bin/bash
cd "$(dirname "$0")"
PORT="${1:-8080}"

LAN_IP=""
if command -v ipconfig &>/dev/null; then
  LAN_IP=$(ipconfig getifaddr en0 2>/dev/null || ipconfig getifaddr en1 2>/dev/null || true)
fi

echo ""
echo "  Kosmos prototype"
echo "  ─────────────────────────────────────"
echo "  On this Mac:"
echo "  → http://localhost:${PORT}"
if [ -n "$LAN_IP" ]; then
  echo ""
  echo "  Share with friends on same WiFi:"
  echo "  → http://${LAN_IP}:${PORT}"
fi
echo ""
echo "  Press Ctrl+C to stop"
echo ""

if command -v python3 &>/dev/null; then
  python3 -m http.server "$PORT"
elif command -v python &>/dev/null; then
  python -m SimpleHTTPServer "$PORT"
else
  echo "Python not found. Install Python or use: npx serve ."
  exit 1
fi
