package com.dut.erp.util;

import com.dut.erp.dto.response.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JsonMapper {

  public static String toJson(Object obj) {
    if (obj instanceof AiSalesForecastResponse forecast) {
      return serializeForecast(forecast);
    } else if (obj instanceof AiInventoryAnalysisResponse inventory) {
      return serializeInventory(inventory);
    } else if (obj instanceof List<?> list) {
      return serializeList(list);
    } else if (obj instanceof AiReorderRecommendationResponse reorder) {
      return serializeReorder(reorder);
    } else if (obj instanceof AiDashboardSummaryResponse dashboard) {
      return serializeDashboard(dashboard);
    }
    throw new IllegalArgumentException("Unknown type to serialize: " + obj.getClass());
  }

  @SuppressWarnings("unchecked")
  public static <T> T fromJson(String json, Class<T> clazz) {
    org.springframework.boot.json.JsonParser parser = org.springframework.boot.json.JsonParserFactory.getJsonParser();
    if (clazz == AiSalesForecastResponse.class) {
      java.util.Map<String, Object> map = parser.parseMap(json);
      return (T) deserializeForecast(map);
    } else if (clazz == AiInventoryAnalysisResponse.class) {
      java.util.Map<String, Object> map = parser.parseMap(json);
      return (T) deserializeInventory(map);
    } else if (clazz == AiReorderRecommendationResponse.class) {
      java.util.Map<String, Object> map = parser.parseMap(json);
      return (T) deserializeReorder(map);
    } else if (clazz == AiDashboardSummaryResponse.class) {
      java.util.Map<String, Object> map = parser.parseMap(json);
      return (T) deserializeDashboard(map);
    }
    throw new IllegalArgumentException("Unknown type to deserialize: " + clazz);
  }

  @SuppressWarnings("unchecked")
  public static <T> T fromJsonList(String json) {
    org.springframework.boot.json.JsonParser parser = org.springframework.boot.json.JsonParserFactory.getJsonParser();
    java.util.List<Object> list = parser.parseList(json);
    return (T) list.stream().map(item -> deserializeProductAbcXyz((java.util.Map<String, Object>) item)).toList();
  }

  private static String escape(String s) {
    if (s == null) return "null";
    return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
  }

  private static String num(BigDecimal d) {
    return d == null ? "null" : d.toString();
  }

  private static String num(Integer i) {
    return i == null ? "null" : i.toString();
  }

  private static BigDecimal toBigDecimal(Object o) {
    if (o == null) return null;
    if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
    return new BigDecimal(o.toString());
  }

  private static Integer toInteger(Object o) {
    if (o == null) return null;
    if (o instanceof Number n) return n.intValue();
    return Integer.parseInt(o.toString());
  }

  private static UUID toUUID(Object o) {
    if (o == null) return null;
    return UUID.fromString(o.toString());
  }

  private static String serializeForecast(AiSalesForecastResponse f) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"summary\":").append(escape(f.summary())).append(",");
    sb.append("\"forecast_30d_total_revenue\":").append(num(f.forecast30dTotalRevenue())).append(",");
    sb.append("\"forecast_points\":[");
    if (f.forecastPoints() != null) {
      sb.append(f.forecastPoints().stream().map(p -> 
        String.format("{\"date\":%s,\"historical_revenue\":%s,\"predicted_revenue\":%s}",
          escape(p.date()), num(p.historicalRevenue()), num(p.predictedRevenue()))
      ).collect(Collectors.joining(",")));
    }
    sb.append("],");
    sb.append("\"insights\":[");
    if (f.insights() != null) {
      sb.append(f.insights().stream().map(JsonMapper::escape).collect(Collectors.joining(",")));
    }
    sb.append("]");
    sb.append("}");
    return sb.toString();
  }

  private static String serializeInventory(AiInventoryAnalysisResponse inv) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"summary\":").append(escape(inv.summary())).append(",");
    sb.append("\"abc_xyz_matrix\":[");
    if (inv.abcXyzMatrix() != null) {
      sb.append(inv.abcXyzMatrix().stream().map(JsonMapper::serializeProductAbcXyz).collect(Collectors.joining(",")));
    }
    sb.append("],");
    sb.append("\"critical_stock_count\":").append(num(inv.criticalStockCount())).append(",");
    sb.append("\"recommendations\":[");
    if (inv.recommendations() != null) {
      sb.append(inv.recommendations().stream().map(JsonMapper::escape).collect(Collectors.joining(",")));
    }
    sb.append("]");
    sb.append("}");
    return sb.toString();
  }

  private static String serializeProductAbcXyz(AiProductAbcXyz p) {
    return String.format(
      "{\"productId\":%s,\"productName\":%s,\"abcClass\":%s,\"xyzClass\":%s,\"currentStock\":%s,\"rop\":%s,\"eoq\":%s,\"status\":%s}",
      p.productId() == null ? "null" : escape(p.productId().toString()),
      escape(p.productName()),
      escape(p.abcClass()),
      escape(p.xyzClass()),
      num(p.currentStock()),
      num(p.rop()),
      num(p.eoq()),
      escape(p.status())
    );
  }

  private static String serializeList(List<?> list) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    sb.append(list.stream().map(item -> {
      if (item instanceof AiProductAbcXyz p) {
        return serializeProductAbcXyz(p);
      }
      throw new IllegalArgumentException("Unknown list item type: " + item.getClass());
    }).collect(Collectors.joining(",")));
    sb.append("]");
    return sb.toString();
  }

  private static String serializeReorder(AiReorderRecommendationResponse r) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"recommendations\":[");
    if (r.recommendations() != null) {
      sb.append(r.recommendations().stream().map(item -> 
        String.format(
          "{\"productId\":%s,\"productName\":%s,\"warehouseId\":%s,\"warehouseName\":%s,\"currentStock\":%s,\"rop\":%s,\"eoq\":%s,\"recommendedQuantity\":%s,\"urgency\":%s,\"notes\":%s}",
          item.productId() == null ? "null" : escape(item.productId().toString()),
          escape(item.productName()),
          item.warehouseId() == null ? "null" : escape(item.warehouseId().toString()),
          escape(item.warehouseName()),
          num(item.currentStock()),
          num(item.rop()),
          num(item.eoq()),
          num(item.recommendedQuantity()),
          escape(item.urgency()),
          escape(item.notes())
        )
      ).collect(Collectors.joining(",")));
    }
    sb.append("]");
    sb.append("}");
    return sb.toString();
  }

  private static String serializeDashboard(AiDashboardSummaryResponse d) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"summary\":").append(escape(d.summary())).append(",");
    sb.append("\"alerts\":[");
    if (d.alerts() != null) {
      sb.append(d.alerts().stream().map(JsonMapper::escape).collect(Collectors.joining(",")));
    }
    sb.append("]");
    sb.append("}");
    return sb.toString();
  }

  @SuppressWarnings("unchecked")
  private static AiSalesForecastResponse deserializeForecast(java.util.Map<String, Object> map) {
    String summary = (String) map.get("summary");
    BigDecimal total = toBigDecimal(map.get("forecast_30d_total_revenue"));
    java.util.List<Object> pointsList = (java.util.List<Object>) map.get("forecast_points");
    java.util.List<ForecastPoint> points = new java.util.ArrayList<>();
    if (pointsList != null) {
      for (Object obj : pointsList) {
        java.util.Map<String, Object> m = (java.util.Map<String, Object>) obj;
        points.add(new ForecastPoint(
          (String) m.get("date"),
          toBigDecimal(m.get("historical_revenue")),
          toBigDecimal(m.get("predicted_revenue"))
        ));
      }
    }
    java.util.List<String> insights = (java.util.List<String>) map.get("insights");
    return new AiSalesForecastResponse(summary, total, points, insights);
  }

  @SuppressWarnings("unchecked")
  private static AiInventoryAnalysisResponse deserializeInventory(java.util.Map<String, Object> map) {
    String summary = (String) map.get("summary");
    java.util.List<Object> matrixList = (java.util.List<Object>) map.get("abc_xyz_matrix");
    java.util.List<AiProductAbcXyz> matrix = new java.util.ArrayList<>();
    if (matrixList != null) {
      for (Object obj : matrixList) {
        matrix.add(deserializeProductAbcXyz((java.util.Map<String, Object>) obj));
      }
    }
    Integer critical = toInteger(map.get("critical_stock_count"));
    java.util.List<String> recommendations = (java.util.List<String>) map.get("recommendations");
    return new AiInventoryAnalysisResponse(summary, matrix, critical, recommendations);
  }

  @SuppressWarnings("unchecked")
  private static AiReorderRecommendationResponse deserializeReorder(java.util.Map<String, Object> map) {
    java.util.List<Object> recList = (java.util.List<Object>) map.get("recommendations");
    java.util.List<AiReorderItem> recs = new java.util.ArrayList<>();
    if (recList != null) {
      for (Object obj : recList) {
        java.util.Map<String, Object> m = (java.util.Map<String, Object>) obj;
        recs.add(new AiReorderItem(
          toUUID(m.get("productId")),
          (String) m.get("productName"),
          toUUID(m.get("warehouseId")),
          (String) m.get("warehouseName"),
          toBigDecimal(m.get("currentStock")),
          toBigDecimal(m.get("rop")),
          toBigDecimal(m.get("eoq")),
          toBigDecimal(m.get("recommendedQuantity")),
          (String) m.get("urgency"),
          (String) m.get("notes")
        ));
      }
    }
    return new AiReorderRecommendationResponse(recs);
  }

  @SuppressWarnings("unchecked")
  private static AiDashboardSummaryResponse deserializeDashboard(java.util.Map<String, Object> map) {
    String summary = (String) map.get("summary");
    java.util.List<String> alerts = (java.util.List<String>) map.get("alerts");
    return new AiDashboardSummaryResponse(summary, alerts);
  }

  private static AiProductAbcXyz deserializeProductAbcXyz(java.util.Map<String, Object> map) {
    return new AiProductAbcXyz(
      toUUID(map.get("productId")),
      (String) map.get("productName"),
      (String) map.get("abcClass"),
      (String) map.get("xyzClass"),
      toBigDecimal(map.get("currentStock")),
      toBigDecimal(map.get("rop")),
      toBigDecimal(map.get("eoq")),
      (String) map.get("status")
    );
  }
}
