# What is this
a library that simplifies data serialization, saving and deserialization... \
and hopefully will simplify data migration. \
This uses JDK 25 \
This library is focused on making Configs easier.

TODO PUT MORE INFO HERE

# TODO
1. add tests
2. Add a codec system so like Codec<SomeObject> codec = new Codec(new Key("KeyID", *sort of Type<T> goes here*, Consumer<T *T of the Type<T>*>)), you can also specify another codec to be included so like new Key("KeyID", Codec)