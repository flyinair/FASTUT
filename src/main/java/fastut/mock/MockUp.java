package fastut.mock;

public class MockUp {

    public static Object tryMock(String normalId, Object... values) {
        Condition condition = new Condition(normalId, values);
        Expect expect = MockPool.getExpect(condition);
        if (expect == null || !expect.isMocked()) {
            condition = new Condition(normalId);
            expect = MockPool.getExpect(condition);
        }
        if (expect != null && expect.isMocked()) {
            return expect.getResult();
        }
        return null;
    }
}
