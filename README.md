# (WIP) Wolfram
Proxy client for android based on [Xray-core], inspired by [SimpleXray].

This is a simple app that does all the necessary work to wire up Xray-core's TUN
inbound to Android's VpnService and provides some nice to have QoL features like
config editing, logs display and geofiles download, among other things.

> [!CAUTION]
> The project is WORK IN PROGRESS, there is no versioning and APKs are unsigned.

> [!WARNING]
> This project was created exclusively for research purposes.
> The author does not condone or promote any unlawful activities.
> Make sure you comply with the local law when using this project.

## Building
Wolfram bundles native xray executables. Before building, make sure to place the
executable renamed to `libxray.so` into `app/src/main/jniLibs/$ARCH`, where ARCH
is your device's architecture. Ability to download core at runtime is planned.

If in trouble, refer to the [github action file](.github/workflows/build.yaml).
Prebuilt APKs are also available as CI artifacts.

## TODOs
- [ ] Make it look good
- [X] Add quick settings tile
- [ ] Add ability to download core binaries
- [X] Settings export/import
- [X] Database migration
- [ ] Balancer stats support
- [ ] Improve config editor (validation, syntax, indent, kb for json chars)
- [ ] Modular configs (maybe?)
- [X] Templated configs
- [ ] Improve logs display (search, freeze, export, highlight?)

## Limitations
To prevent sending traffic into an andless loop of despair, Wolfram itself is
excluded from the VPN tunnel. This causes traffic originating from Wolfram, such
as downloading geofiles, to bypass the tunnel. This can be an issue if access to
services hosting the desired files is restricted.
I don't know how to deal with this yet.

## License
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

## LLM usage disclosure
LLMs were used to write pieces of UI code. Nobody likes writing UI code, sue me

## References
- https://xtls.github.io/config/inbounds/tun.html
- https://developer.android.com/reference/android/net/VpnService

[Xray-core]: https://github.com/XTLS/Xray-core
[SimpleXray]: https://github.com/lhear/SimpleXray

