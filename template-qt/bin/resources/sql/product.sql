<sqls namespace="Product">
    <define id="fields">id, name</define>

    <sql id="selectById">
        SELECT <include defineId="fields"/> FROM product WHERE id=%1
    </sql>

    <sql id="selectAll">
        SELECT id, name FROM product
    </sql>
</sqls>
