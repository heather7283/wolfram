#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/un.h>
#include <unistd.h>
#include <assert.h>

#include "common.h"

static int get_fd(const char *socket_path) {
    int fd = -1;

    int sock = socket(AF_UNIX, SOCK_SEQPACKET, 0);
    if (sock < 0) {
        perror("could not create socket");
        goto out;
    }

    struct sockaddr_un addr;
    socklen_t addr_len = make_abstract_addr(&addr, socket_path);
    if (connect(sock, (const struct sockaddr *)&addr, addr_len) < 0) {
        perror("could not connect to socket");
        goto out;
    }

    char dummy;
    struct iovec iov = { .iov_base = &dummy, .iov_len = sizeof(dummy) };

    char cmsg_buf[CMSG_SPACE(sizeof(int))];
    memset(cmsg_buf, '\0', sizeof(cmsg_buf));

    struct msghdr msg = {
        .msg_iov = &iov,
        .msg_iovlen = 1,
        .msg_control = cmsg_buf,
        .msg_controllen = sizeof(cmsg_buf),
    };

    if (recvmsg(sock, &msg, 0) <= 0) {
        perror("could not receive message");
        goto out;
    }

    for (struct cmsghdr *cmsg = CMSG_FIRSTHDR(&msg); cmsg; cmsg = CMSG_NXTHDR(&msg, cmsg)) {
        if (cmsg->cmsg_level == SOL_SOCKET && cmsg->cmsg_type == SCM_RIGHTS) {
            memcpy(&fd, CMSG_DATA(cmsg), sizeof(fd));
            break;
        }
    }
    if (fd < 0) {
        fprintf(stderr, "no SCM_RIGHTS message found\n");
    }

out:
    close(sock);
    return fd;
}

int main(int _, char **argv) {
    char **core_argv = &argv[1];

    // TODO: extremely ugly hack, do something about it (when I get this whole contraption working)
    int fd = -1;
    for (int retries = 5; fd < 0 && retries--; sleep(1)) {
        fd = get_fd(WOLFRAM_SOCKET_NAME);
    }
    if (fd < 0) {
        fprintf(stderr, "failed to receive tun fd\n");
        return 1;
    }

    char *var = malloc(64);
    snprintf(var, 64, "XRAY_TUN_FD=%d", fd);
    if (putenv(var) != 0) {
        perror("putenv");
        return 1;
    }

    execv(core_argv[0], core_argv);

    perror("execv");
    return 1;
}
