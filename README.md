# (WIP) Wolfram
Proxy client for android based on [Xray-core], inspired by [SimpleXray].

> [!WARNING]
> This project was created exclusively for research purposes.
> The author does not condone or promote any unlawful activities.
> Make sure you comply with the local law when using this project.

## Why?
I wanted a dumb wrapper around Xray-core for android that won't get in my way.

Unlike the aforementioned project that relies on a tun2socks library,
Wolfram uses Xray-core's TUN inbound feature to directly interact with
Android's VpnService API, which should be more efficient in theory.

## TODOs:
- [ ] Make it look good
- [ ] Add quick settings menu widget
- [ ] Add ability to download core binaries
- [ ] Settings export/import
- [ ] Database migration
- [ ] Balancer stats support
- [ ] Improve config editor (validation, syntax, indent, kb for json chars)
- [ ] Modular configs (maybe?)
- [ ] Templated configs (maybe?)
- [ ] Improve logs display (search, freeze, export, highlight?)

## References:
- https://github.com/android/architecture-samples
- https://github.com/android/compose-samples
- https://github.com/android/snippets

[Xray-core]: https://github.com/XTLS/Xray-core
[SimpleXray]: https://github.com/lhear/SimpleXray

