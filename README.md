# gitiles

~~~bash
git clone https://gerrit.googlesource.com/gitiles

cd gitiles

git checkout v1.5.0

git submodule update --init

cd modules/jgit

mvn install
~~~