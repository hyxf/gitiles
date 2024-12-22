# gitiles

A simple browser for Git repositories

## Install 

~~~bash
brew install hyxf/cli/gitiles
~~~

## Develop 

~~~bash
git clone https://gerrit.googlesource.com/gitiles

cd gitiles

git checkout v1.5.0

git submodule update --init

cd modules/jgit

mvn install
~~~

## sha256

~~~bash
shasum -a 256 filename
~~~