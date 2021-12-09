package com.jqp.admin.common;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CrudData<T> {
    private int count;
    private List<T> rows = new ArrayList<>();
    private List<ColumnData> columns = new ArrayList<>();
}
