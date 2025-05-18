package org.jetc.utils.errors.result;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Results<T,E> {
    private final Result.Ok<T,E> ok;
    private final Result.Err<T,E> err;

    private Results(Result<T, E> result) {
        switch (result){
            case Result.Err<T, E> v -> {
                err = v;
                ok = null;
            }
            case Result.Ok<T, E> v -> {
                err = null;
                ok = v;
            }
        }
    }


    public static <T,E> Result.Ok<T, E> ok(T value){
        return new Result.Ok<>(value);
    }

    public static <T,E> Result.Err<T, E> error(E content){
        return new Result.Err<>(content);
    }


    public static <T,E> Results<T,E> wrap(Result<T,E> r) {
        return new Results<>(r);
    }

    public Result<T,E> unwrap() {
        return ok != null ? ok : err;
    }

    public void ifOk(Consumer<T> consumer) {
        if(ok != null){
            consumer.accept(ok.value());
        }
    }

    public void ifErr(Consumer<E> consumer) {
        if(err != null){
            consumer.accept(err.error());
        }
    }

    public <U> Results<U,E> map(Function<? super T, ? extends U> mapper){
        Result<U,E> result;
        if(ok != null) {
            result = new Result.Ok<>(
                mapper.apply(ok.value())
            );
        } else {
            result = new Result.Err<>(
                Objects.requireNonNull(err).error()
            );
        }

        return Results.wrap(result);
    }

    public <U> Results<T,U> mapErr(Function<? super E, ? extends U> mapper){
        Result<T,U> result;
        if(err != null) {
            result = new Result.Err<>(
                mapper.apply(err.error())
            );
        } else {
            result = new Result.Ok<>(
                Objects.requireNonNull(ok).value()
            );
        }

        return Results.wrap(result);
    }

    public Results<T,E> or(Function<E, ? extends Result<? extends T, ? extends E>> errToOk){
        if(ok != null){
            return this;
        } else {
            @SuppressWarnings("unchecked")
            Result<T,E> recovered = (Result<T, E>) errToOk.apply(Objects.requireNonNull(err).error());
            return Results.wrap(recovered);
        }
    }

    public T getValue() {
        return ok != null ? ok.value() : null;
    }

    public E getErr() {
        return err != null ? err.error() : null;
    }
}
