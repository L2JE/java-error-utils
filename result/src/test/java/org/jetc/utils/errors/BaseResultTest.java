package org.jetc.utils.errors;

import org.jetc.utils.errors.result.Result;
import org.jetc.utils.errors.result.Results;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class BaseResultTest {
    @Test
    void inSwitchExpression(){
        Result<MyObjectExample, MyError> r = myOtherMethod(true, "999", "asdf", 234);

        switch (r){
            case Result.Err(MyError err) -> handleError(err);
            case Result.Ok(MyObjectExample value) -> handleSuccess(value);
        }
    }

    @Test
    void ifOkMethod(){
        Result<MyObjectExample, MyError> r = myOtherMethod(true, "999", "asdf", 234);
        Results.wrap(r).ifOk(this::handleSuccess);
    }

    @Test
    void ifErrMethod(){
        Result<MyObjectExample, MyError> r = myOtherMethod(true, "999", "asdf", 234);
        Results.wrap(r).ifErr(this::handleError);
    }

    @Test
    void mapOk(){
        Results<MyObjectExample2, MyError> a = Results.wrap(myOtherMethod(true, "999", "asdf", 234)).map(res -> new MyObjectExample2(res.data2()));
        assertEquals(new MyObjectExample2(234), a.getValue());
        assertNull(a.getErr());
    }

    @Test
    void mapErr(){
        Results<MyObjectExample, MyError2> a = Results.wrap(myOtherMethod(false, "999", "asdf", 234)).mapErr(err -> new MyError2());
        assertNotNull(a.getErr());
        assertNull(a.getValue());
    }

    @Test
    void recoverOkFromError(){
        Results<MyObjectExample, MyError> result = Results.wrap(myOtherMethod(false, "999", "asdf", 234)).or(err -> {
            if(err.number().equals("999"))
                return Results.ok(new MyObjectExample("234", 123));

            return Results.error(new MyError("Unexpected Value"));
        });

        Result.Ok okRes = assertInstanceOf(Result.Ok.class, result.unwrap());
        assertEquals(new MyObjectExample("234", 123), okRes.value());
    }

    @Test
    void unableToRecoverOkFromError(){
        Results<MyObjectExample, MyError> result = Results.wrap(myOtherMethod(false, "123", "asdf", 234)).or(err -> {
            if(err.number().equals("999"))
                return Results.ok(new MyObjectExample("234", 123));

            return Results.error(new MyError("Unexpected Value"));
        });

        Result.Err errRes = assertInstanceOf(Result.Err.class, result.unwrap());
        assertEquals(new MyError("Unexpected Value"), errRes.error());
    }

    @Test
    void unnecessaryRecoverOkFromError(){
        Results<MyObjectExample, MyError> result = Results.wrap(myOtherMethod(true, "123", "asdf", 234)).or(err -> {
            if(err.number().equals("999"))
                return Results.ok(new MyObjectExample("234", 123));

            return Results.error(new MyError("Unexpected Value"));
        });

        Result.Ok okRes = assertInstanceOf(Result.Ok.class, result.unwrap());
        assertEquals(new MyObjectExample("asdf", 234), okRes.value());
    }

    private void handleError(MyError err) {}

    private void handleSuccess(MyObjectExample value) {}

    Result<MyObjectExample, MyError> myOtherMethod(boolean success, String errVal, String successData1, int successData2){
        return success ? Results.ok(new MyObjectExample(successData1, successData2)) : Results.error(new MyError(errVal));
    }

    public record MyObjectExample(String data1, int data2){}
    public record MyObjectExample2(int data2){}

    public record MyError(String number){}
    public record MyError2(){}
}