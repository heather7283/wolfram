#pragma once

#define _3(a, b) a##b
#define _2(a, b) _3(a, b)
#define _ _2(_dummy_param_, __COUNTER__)

#define WOLFRAM_SOCKET_NAME "@io.github.heather7283.wolfram.socket"

static socklen_t make_abstract_addr(struct sockaddr_un *addr, const char *name) {
    memset(addr, 0, sizeof(*addr));
    addr->sun_family = AF_UNIX;
    strncpy(addr->sun_path + 1, name + 1, sizeof(addr->sun_path) - 2);

    return (socklen_t)(offsetof(struct sockaddr_un, sun_path) + 1 + strlen(name + 1));
}
