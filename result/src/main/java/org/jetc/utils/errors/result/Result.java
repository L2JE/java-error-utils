package org.jetc.utils.errors.result;

public sealed interface Result<T,E> permits Result.Err, Result.Ok {
    record Ok<T,E>(T value) implements Result<T,E>{}
    record Err<T,E>(E error) implements Result<T,E>{}
}
