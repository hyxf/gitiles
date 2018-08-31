#!/usr/local/bin/python3 -u
# -*- coding: utf-8 -*-
"""
git sync tools
"""

import argparse
import os
import re
import time
from urllib import parse

__version__ = '1.0.0'


def _inter_git_dir(input_dir, git_dir_list, mirror=False):
    """inter git dir
    :param input_dir:
    :param git_dir_list:
    :param mirror:
    :return:
    """
    sub_dir_list = [x for x in os.listdir(input_dir) if os.path.isdir(os.path.join(input_dir, x))]
    if "branches" in sub_dir_list and "objects" in sub_dir_list:
        if mirror:
            git_dir_list.append(input_dir)
    elif ".git" in sub_dir_list:
        if not mirror:
            git_dir_list.append(input_dir)
    elif ".svn" in sub_dir_list:
        pass
    else:
        for sub_dir in sub_dir_list:
            _inter_git_dir(os.path.join(input_dir, sub_dir), git_dir_list, mirror=mirror)


def get_git_dir(input_dir, mirror=False, filter_host=['']):
    """get git dir list
    :param input_dir:
    :param mirror:
    :param filter_host:
    :return:
    """
    temp_list = []
    remote_list = []
    git_list = []
    _inter_git_dir(input_dir=input_dir,
                   git_dir_list=temp_list,
                   mirror=mirror)
    for git_dir in temp_list:
        os.chdir(git_dir)
        cmd_result = os.popen('git remote -v').read()
        origin_list = re.findall(r'origin	(.*?) \(push\)', cmd_result)
        if origin_list:
            origin = origin_list[0].strip()
            # -------
            if origin.startswith('git'):
                temp_host = re.findall(r'@(.*?):', origin)
                if temp_host:
                    host = temp_host[0].strip()
            elif origin.startswith('http'):
                parse_result = parse.urlparse(url=origin)
                """:type: urllib.parse.ParseResult"""
                host = parse_result.hostname
            else:
                pass
            if host not in remote_list:
                remote_list.append(host)
            # -------
            is_ok = True
            for host in filter_host:
                if host in origin:
                    is_ok = False
                    break
            if is_ok:
                git_list.append(git_dir)
    return remote_list, git_list


def cmd_fetch(git_dir):
    """git fetch
    :param git_dir:
    :return:
    """
    os.chdir(git_dir)
    os.system('git fetch origin')


def cmd_pull(git_dir):
    """git pull
    :param git_dir:
    :return:
    """
    os.chdir(git_dir)
    os.system('git pull origin')


def my_print(message):
    """
    print
    :param message:
    :return:
    """
    print(message)


def git_sync(args):
    """git sync
    :param args:
    :return:
    """
    # target dir
    target_dir = args.target_dir
    # filter host
    filter_host = args.filter_host
    # mirror
    mirror = not args.not_mirror
    # list remote
    list_remote = args.list_remote
    start_time = time.time()
    my_print('>> Start: {0}'.format(time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(start_time))))
    remote_list, git_list = get_git_dir(input_dir=target_dir,
                                        mirror=mirror,
                                        filter_host=filter_host)
    if list_remote:
        if remote_list:
            size = len(remote_list)
            for index, remote in enumerate(remote_list):
                my_print('[{0}/{1}] - {2}'.format(index + 1, size, remote))
        else:
            my_print('>> no remote')
    else:
        size = len(git_list)
        for index, git_dir in enumerate(git_list):
            my_print('[{0}/{1}] - {2}'.format(index + 1, size, git_dir.replace(target_dir, '')))
            if mirror:
                cmd_fetch(git_dir=git_dir)
            else:
                cmd_pull(git_dir=git_dir)
    end_time = time.time()
    my_print('>> End {0}'.format(time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(end_time))))
    run_time = end_time - start_time
    my_print('>> Time: {0}'.format(run_time))


def execute():
    """execute point
    :return:
    """
    parser = argparse.ArgumentParser(description='git sync', epilog='make it easy')
    parser.add_argument('-d', '--target_dir', type=str, default='/Users/hyxf/mirror/', help='target dir')
    parser.add_argument('-f', '--filter_host', action='append', default=['googlesource.com'], help='filter host')
    parser.add_argument('-n', '--not_mirror', help='git mirror ?', action='store_true', default=False)
    # show remote list
    parser.add_argument('-l', '--list_remote', help='show remote list', action='store_true', default=False)
    parser.set_defaults(func=git_sync)
    # parser args
    args = parser.parse_args()
    args.func(args)


if __name__ == '__main__':
    # sys.argv.append('-l')
    execute()
