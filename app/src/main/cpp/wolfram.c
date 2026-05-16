#include <jni.h>
#include <android/log.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <stdbool.h>
#include <errno.h>
#include <string.h>
#include <assert.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <sys/select.h>

#include "common.h"

#define LOG(prio, tag, fmt, ...) \
    __android_log_buf_print(LOG_ID_MAIN, ANDROID_LOG_##prio, tag, fmt, ##__VA_ARGS__)

JNIEXPORT jboolean JNICALL
Java_io_github_heather7283_wolfram_vpn_WolframVpnService_sendFd(JNIEnv *_, jobject _, jint fd) {
    jboolean ret = false;
    int conn = -1, sock = -1;

    sock = socket(AF_UNIX, SOCK_SEQPACKET, 0);
    if (sock < 0) {
        LOG(ERROR, "sendFd", "could not create socket: %s", strerror(errno));
        goto out;
    }

    struct sockaddr_un addr;
    socklen_t addr_len = make_abstract_addr(&addr, WOLFRAM_SOCKET_NAME);

    if (bind(sock, (struct sockaddr *)&addr, addr_len) < 0) {
        LOG(ERROR, "sendFd", "could not bind socket: %s", strerror(errno));
        goto out;
    }
    if (listen(sock, 1) < 0) {
        LOG(ERROR, "sendFd", "could not listen on socket: %s", strerror(errno));
        goto out;
    }

    fd_set rfds;
    FD_ZERO(&rfds);
    FD_SET(sock, &rfds);
    switch (select(sock + 1, &rfds, NULL, NULL, &(struct timeval){ .tv_sec = 3 })) {
    case -1:
        LOG(ERROR, "sendFd", "select failed: %s", strerror(errno));
        goto out;
    case 0:
        LOG(ERROR, "sendFd", "socket connection timeout");
        goto out;
    }

    conn = accept(sock, NULL, NULL);
    if (conn < 0) {
        LOG(ERROR, "sendFd", "could not accept connection: %s", strerror(errno));
        goto out;
    }

    char dummy = 67;
    struct iovec iov = { .iov_base = &dummy, .iov_len = 1 };

    char cmsg_buf[CMSG_SPACE(sizeof(int))];
    memset(cmsg_buf, '\0', sizeof(cmsg_buf));

    struct msghdr msg = {
        .msg_iov = &iov,
        .msg_iovlen = 1,
        .msg_control = cmsg_buf,
        .msg_controllen = sizeof(cmsg_buf),
    };

    struct cmsghdr *cmsg = CMSG_FIRSTHDR(&msg);
    cmsg->cmsg_level = SOL_SOCKET;
    cmsg->cmsg_type = SCM_RIGHTS;
    cmsg->cmsg_len = CMSG_LEN(sizeof(fd));
    memcpy(CMSG_DATA(cmsg), &fd, sizeof(fd));

    if (sendmsg(conn, &msg, 0) < 0) {
        LOG(ERROR, "sendFd", "could not send message: %s", strerror(errno));
        goto out;
    }

    ret = true;

out:
    close(conn);
    close(sock);
    return ret;
}