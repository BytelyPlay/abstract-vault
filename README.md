# What is this
a library that simplifies data saving...
and hopefully will simplify data migration.
TODO PUT MORE INFO HERE

# TODO
1. add tests
2. Add an option to give class hints so we don't need the "class": "whatever" thing and maybe if no class hint was given we can put the id and then the class inside of the id so like {"id": "class": "data"} if that makes sense
3. Add a codec system so like Codec<SomeObject> codec = new Codec(new Key("KeyID", *sort of Type<T> goes here*, Consumer<T *T of the Type<T>*>)), you can also specify another codec to be included so like new Key("KeyID", Codec)