package com.jqp.admin.flow.graphData;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Edge {
    private String id;
    private String type;
    private String sourceNodeId;
    private String targetNodeId;
    private EdgeProperties properties = new EdgeProperties();
    private NodeText text = new NodeText();

    //位置信息,忽略
    private Point startPoint = new Point();
    private Point endPoint = new Point();
    private List<Point> pointsList = new ArrayList<>();
}
