# Video Raccoon

An early-stage middleware that connects to multiple Video Management Systems (VMS) and
exposes one canonical, vendor-agnostic API — for both control and live video — so client
applications only ever speak one protocol, regardless of which VMS vendor is actually
behind a given camera.

## Why

VMS platforms don't agree on protocols, SDKs, or data models. Wiring each one directly
into a client app doesn't scale — every new vendor means new protocol knowledge baked
into every client that needs it. This project moves that integration work into one
standalone service instead: add a vendor here once, and every client gets it without any
changes on their side.

## Architecture at a glance

```
 Desktop client   ─┐
                    ├──►   Middleware   ──►  Vendor adapter (Camel)  ──►  VMS
 Web client        ─┘         │
                               ├── Control API (REST/JSON): camera metadata,
                               │   stream requests, events, auth
                               └── Video stream transport (WebSocket): one canonical
                                   way to consume live video, any vendor
```

Clients never talk to a VMS directly — for control calls or for video. The middleware
proxies/relays video in passthrough mode (no decode/re-encode) rather than handing
clients a direct URL into the VMS, so the VMS stays fully hidden and the middleware
remains the single point of control over an active stream (auth, revocation, audit).

## Adding VMS vendors: why Apache Camel

Supporting more VMS vendors over time — without ever changing the client-facing API —
is a core design goal, so the integration layer matters more than usual here. Each
vendor is implemented as its own Apache Camel component/route set that talks to that
vendor's native network protocol (REST/SOAP/RTSP/proprietary — not vendor SDKs, which
would reintroduce a language dependency) and translates it into the shared canonical
model.

Camel was chosen for this specifically because it's built for this class of problem:

- **Built for protocol mediation, not just data mapping.** The real challenge across VMS
  vendors isn't different JSON shapes — it's genuinely different transports (SOAP, raw
  TCP, REST, RTSP, proprietary WebSocket framing). Camel's Component SPI gives a
  consistent, well-documented shape for every new vendor adapter.
- **Based on Enterprise Integration Patterns** — a long-established, well-documented
  catalog of patterns for exactly this kind of system integration, rather than
  ad hoc/one-off protocol glue per vendor.
- **Mature, widely-used, well-documented.** An Apache Software Foundation project with
  a large community and extensive documentation — adding a vendor means writing a route,
  not inventing infrastructure.

Camel is deliberately scoped to the integration layer only (protocol translation, video
relay) — not adopted as a general application framework.

## Video relay: go2rtc (candidate)

**Not yet a final decision.** [go2rtc](https://github.com/AlexxIT/go2rtc) is the leading
candidate for the actual media-plane relay: a standalone process the middleware would
control over HTTP/REST, handling protocol-to-protocol video relay so Camel routes don't
have to. It's attractive here because it's:

- **Lightweight and passthrough-first** — relays already-compressed frames without
  decoding/re-encoding, keeping latency and CPU load low.
- **Broad protocol support** — RTSP, WebRTC, HLS, MJPEG, and more, in and out, without
  writing a separate client for each.
- **Actively developed open source**, with real adoption in the self-hosted
  camera/security space.

Adoption is pending a check on whether the target VMS can expose a plain RTSP source for
go2rtc to ingest directly.

## Client-agnostic by design

The canonical API is a plain network service (REST/JSON control, WebSocket video), so it
can serve a desktop client today and a web client later without any API redesign —
client language/stack is irrelevant to the middleware.

## Open source by design

Every component this middleware is built on is open source with an active community and
license terms that support redistribution — Apache Camel (Apache License 2.0) and, as a
candidate, go2rtc (MIT). Vendor integrations talk to each VMS's native network protocol
directly rather than a vendor SDK, so there's no proprietary dependency tying the project
to a single language or platform. That combination — mature, well-documented, permissively
licensed components, no proprietary SDKs — is deliberate, and keeps the door open to
releasing this as a standalone open source project down the line.

## Open decisions

Still unresolved, tracked as the project evolves:

- **Video relay component** — go2rtc is the leading candidate but not yet confirmed.
- **Client↔middleware authentication/authorization** — not designed yet.
- **Packaging** — whether the video-relay layer ships as a separate, reusable component
  from the control-plane API, or as one project.

