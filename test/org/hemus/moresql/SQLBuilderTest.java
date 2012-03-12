package org.hemus.moresql;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SQLBuilderTest  {

    static public class SQLBuilder {

        private final static SQLBuilder EMPTY = new SQLBuilder("");

        private static class SQLBasicBuilder extends SQLBuilder {

            protected final SQLBuilder lhs;
            protected final SQLBuilder rhs;
            protected final String suffix;

            public SQLBasicBuilder(SQLBuilder lhs, String prefix, SQLBuilder rhs, String suffix) {
                super(prefix);
                this.lhs = lhs;
                this.rhs = rhs;
                this.suffix = suffix;
            }

            @Override
            protected StringBuilder build(StringBuilder buffer) {
                lhs.build(buffer);
                return rhs.build(buffer.append(this.prefix)).append(suffix);
            }

        }

        private class SQLWhereClauseBuilder extends SQLSearchConditionBuilder {
            public SQLWhereClauseBuilder(SQLBuilder lhs) {
                super(lhs, "", EMPTY, " where");
            }
        }

        private static class SQLBooleanFactorBuilder extends SQLBooleanTermBuilder {

            public SQLBooleanFactorBuilder(SQLBuilder lhs, String prefix, SQLBuilder rhs, String suffix) {
                super(lhs, prefix, rhs, suffix);
            }

        }

        private static class SQLSearchConditionBuilder extends SQLBasicBuilder {

            public SQLSearchConditionBuilder(SQLBuilder lhs, String prefix, SQLBuilder rhs, String suffix) {
                super(lhs, prefix, rhs, suffix);
            }

            public SQLBooleanTermBuilder not() {
                return new SQLBooleanFactorBuilder(this.lhs, "not (", this, ")");
            }

            public SQLBooleanTermBuilder and() {
                return new SQLBooleanTermBuilder(this.lhs, " and ", EMPTY, "");
            }

            // public SQLBooleanTermBuilder lessThan(String field, Number n) {
            // return new SQLBooleanTermBuilder(this.lhs, " ", EMPTY, field + " < " + n);
            // }
            //
            // public SQLBooleanTermBuilder greaterThan(String field, Number n) {
            // return new SQLBooleanTermBuilder(this.lhs, " ", EMPTY, field + " > " + n);
            // }

        }

        private static class SQLBooleanTermBuilder extends SQLSearchConditionBuilder {

            private SQLBooleanTermBuilder(SQLBuilder lhs, String prefix, SQLBuilder rhs, String suffix) {
                super(lhs, prefix, rhs, suffix);
            }

            @Override
            public SQLSearchConditionBuilder or() {
                return new SQLSearchConditionBuilder(this.lhs, " (", this.rhs, ") or");
            }
        }

        protected final String prefix;

        public SQLBuilder(String prefix) {
            this.prefix = prefix;
        }

        // public SQLBuilder between(String field, WhoDatTable range) {
        // buffer
        // .append('(')
        // .append(field)
        // .append(">=")
        // .append(range.tableRangeFloor())
        // .append(" AND ")
        // .append(field)
        // .append(field)
        // .append(" < ")
        // .append(range.tableRangeCeiling())
        // .append(')');
        // return this;
        // }

        public SQLWhereClauseBuilder where() {
            return new SQLWhereClauseBuilder(this);
        }

        @Override
        public String toString() {
            return build(new StringBuilder()).toString();
        }

        protected StringBuilder build(StringBuilder buffer) {
            return buffer.append(this.prefix);
        }
    }

    @Test
    public void testBasic() throws Exception {
        SQLBuilder b = new SQLBuilder("select");
        assertEquals("select", b.toString());
    }

    @Test
    public void testWhereClause() throws Exception {
        SQLBuilder b = new SQLBuilder("select").where();
        assertEquals("select where", b.toString());
    }

    @Test
    public void testSearchCondition() throws Exception {
        SQLBuilder b = new SQLBuilder("select").where().lessThan("A", 5);
        assertEquals("select where A < 5", b.toString());
    }

    @Test
    public void testDisjunctiveSearchCondition() throws Exception {
        SQLBuilder b = new SQLBuilder("select").where().lessThan("A", 5).or().greaterThan("A", 7);
        assertEquals("select where A < 5 or A > 7", b.toString());
    }

    @Test
    public void testDisjunctiveSearchConditionTwo() throws Exception {
        SQLBuilder b = new SQLBuilder("select").where().lessThan("A", 5).or().greaterThan("A", 7).or().greaterThan("B", 1);
        assertEquals("select where A < 5 or A > 7 or B > 1", b.toString());
    }

    @Test
    public void testConjunctiveSearchCondition() throws Exception {
        SQLBuilder b = new SQLBuilder("select").where().lessThan("A", 5).and().greaterThan("A", 7);
        assertEquals("select where A < 5 and A > 7", b.toString());
    }

    @Test
    public void testMixedSearchCondition() throws Exception {
        SQLBuilder b = new SQLBuilder("select").where().lessThan("A", 5).and().greaterThan("A", 7).or().lessThan("A", 0);
        assertEquals("select where (A < 5 and A > 7) or A < 0", b.toString());
    }

    // public void testAndCouple() throws Exception {
    // SQLBuilder b = new SQLBuilder("select").where().and().lessThan("A", 5).greaterThan("A",
    // 10);
    // assertEquals("select (A < 5 AND A > 10)", b.toString());
    // }
    //
    // public void testOrSingle() throws Exception {
    // SQLBuilder b = new SQLBuilder("select").where().or().lessThan("A", 5);
    // assertEquals("select (A < 5)", b.toString());
    // }
    //
    // public void testOrCouple() throws Exception {
    // SQLBuilder b = new SQLBuilder("select").where().or().lessThan("A", 5).greaterThan("A",
    // 10);
    // assertEquals("select (A < 5 OR A > 10)", b.toString());
    // }
    //
    // public void testAndDisjunctions() throws Exception {
    // SQLBuilder b = new SQLBuilder("select").where().and().lessThan("A", 5).greaterThan("A",
    // 10).or().greaterThan("A", 20);
    // assertEquals("select where (A < 5 AND A > 10)", b.toString());
    // }
}
