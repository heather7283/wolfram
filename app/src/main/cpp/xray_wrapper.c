#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/un.h>
#include <unistd.h>
#include <assert.h>

static socklen_t mkaddr(struct sockaddr_un *addr, const char *name) {
    memset(addr, 0, sizeof(*addr));
    addr->sun_family = AF_UNIX;
    strncpy(addr->sun_path + 1, name, sizeof(addr->sun_path) - 2);

    return (socklen_t)(offsetof(struct sockaddr_un, sun_path) + 1 + strlen(name));
}

static int get_fd(const char *socket_path) {
    int fd = -1;

    int sock = socket(AF_UNIX, SOCK_STREAM, 0);
    if (sock < 0) {
        perror("could not create socket");
        goto out;
    }

    struct sockaddr_un addr;
    socklen_t addr_len = mkaddr(&addr, socket_path);
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

    ssize_t msglen = recvmsg(sock, &msg, 0);
    if (msglen < 0) {
        perror("could not receive message");
        goto out;
    } else if (msglen == 0) {
        fprintf(stderr, "received message of length 0\n");
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
    const char *sock_name = argv[1];
    char **core_argv = &argv[2];

    int fd = get_fd(sock_name);
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
