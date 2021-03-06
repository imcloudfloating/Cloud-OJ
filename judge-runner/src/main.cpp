#include <iostream>
#include <sstream>
#include <cstdlib>
#include <getopt.h>
#include <cstring>
#include "runner.h"

char *cmd[16] = {};
char *work_dir = nullptr;
char *data_dir = nullptr;

Config config;

option long_options[] = {
        {"cmd",         required_argument, nullptr, 'c'},
        {"time",        required_argument, nullptr, 't'},
        {"memory",      required_argument, nullptr, 'm'},
        {"max-memory",  required_argument, nullptr, 'M'},
        {"output-size", required_argument, nullptr, 'o'},
        {"workdir",     required_argument, nullptr, 'w'},
        {"data",        required_argument, nullptr, 'd'},
        {"proc",        required_argument, nullptr, 'p'},
        {"help",        no_argument,       nullptr, 'h'},
        {nullptr,       no_argument,       nullptr, 0}
};

const char *short_options = "c:t:m:M:o:w:d:p:?h";

void show_help();

void get_args(int argc, char *argv[]);

inline long str_to_long(char *str) {
    return strtol(str, nullptr, 10);
}

inline void split(char **arr, char *str, const char *del) {
    char *s;
    s = strtok(str, del);

    while (s != nullptr) {
        *arr++ = s;
        s = strtok(nullptr, del);
    }

    *arr = nullptr;
}

int main(int argc, char *argv[]) {
    get_args(argc, argv);
    RTN rtn = exec(cmd, work_dir, data_dir, config);

    if (rtn.code == 0) {
        std::cout << rtn.result;
    }

    return rtn.code;
}

inline void show_help() {
    const char *fmt = "\t%-16s\t%s\n";
    printf("选项:\n");
    printf(fmt, "-c, --cmd", "命令(空格用 '@' 表示)");
    printf(fmt, "-t, --time", "时间限制(ms)");
    printf(fmt, "-m, --memory", "内存限制(MB)");
    printf(fmt, "-M, --max-memory", "内存上限(MB)");
    printf(fmt, "-o, --output-size", "输出限制(MB)");
    printf(fmt, "-w, --workdir", "工作目录");
    printf(fmt, "-d, --data", "测试数据目录");
    printf(fmt, "-p, --proc", "进程数量限制(可选，默认为 1)");
    printf(fmt, "-h, --help", "显示此信息");
}

/**
 * @brief 解析参数
 */
void get_args(int argc, char *argv[]) {
    int index = 0, opt, cnt = 0;

    while ((opt = getopt_long_only(argc, argv, short_options, long_options, &index)) != EOF) {
        switch (opt) {
            case 'c':
                split(cmd, optarg, "@");
                cnt++;
                break;
            case 't':
                config.timeout = str_to_long(optarg);
                cnt++;
                break;
            case 'm':
                config.memory = str_to_long(optarg) << 10;
                cnt++;
                break;
            case 'M':
                config.max_memory = str_to_long(optarg) << 10;
                cnt++;
                break;
            case 'o':
                config.output_size = str_to_long(optarg) << 10;
                cnt++;
                break;
            case 'w':
                work_dir = optarg;
                cnt++;
                break;
            case 'd':
                data_dir = optarg;
                cnt++;
                break;
            case 'p':
                config.proc_count = (int) str_to_long(optarg);
                break;
            case 'h':
                show_help();
                exit(0);
            case ':':
            default:
                exit(JUDGE_ERROR);
        }
    }

    if (cnt < 7) {
        fprintf(stderr, "%s: Missing option(s)", argv[0]);
        exit(JUDGE_ERROR);
    }
}
