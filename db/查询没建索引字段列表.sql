
SELECT
    t.TABLE_NAME,
    t.TABLE_COMMENT,
    c.column_name,
    c.column_comment,
    CONCAT(t.TABLE_NAME,'_',c.COLUMN_NAME,'_fk') fk
FROM
    information_schema.`TABLES` t ,
    information_schema.`COLUMNS` c
WHERE
        t.TABLE_SCHEMA = 'jqp'
  and c.TABLE_SCHEMA = 'jqp'
  and c.table_name = t.table_name
  and POSITION('_' in c.column_name) <> 0
  and POSITION('id' in c.column_name) <> 0
  and c.COLUMN_NAME like '%id%'
  and c.COLUMN_NAME <> 'field_width'
  and c.TABLE_NAME <> 'spring_session'
  and c.TABLE_NAME <> 'spring_session_attributes'
  and (c.TABLE_NAME = 'enterprise_user' and c.column_name <> 'dept_id')


  and CONCAT(t.TABLE_NAME,'_',c.COLUMN_NAME,'_fk') not in (
    select
        s.CONSTRAINT_NAME
    from INFORMATION_SCHEMA.KEY_COLUMN_USAGE s
             left join information_schema.`TABLES` t on t.TABLE_NAME = s.TABLE_NAME and t.TABLE_SCHEMA = s.TABLE_SCHEMA
             left join information_schema.`COLUMNS` tc on tc.TABLE_NAME = s.TABLE_NAME and tc.TABLE_SCHEMA = s.TABLE_SCHEMA and tc.COLUMN_NAME = s.COLUMN_NAME
             left join information_schema.`TABLES` r on r.TABLE_NAME = s.REFERENCED_TABLE_NAME and r.TABLE_SCHEMA = s.TABLE_SCHEMA
             left join information_schema.`COLUMNS` rc on rc.TABLE_NAME = s.REFERENCED_TABLE_NAME and rc.TABLE_SCHEMA = s.TABLE_SCHEMA and rc.COLUMN_NAME = s.		REFERENCED_COLUMN_NAME
    where s.table_schema ='jqp'
      and s.REFERENCED_COLUMN_NAME is not null
)
ORDER BY
    table_name ASC