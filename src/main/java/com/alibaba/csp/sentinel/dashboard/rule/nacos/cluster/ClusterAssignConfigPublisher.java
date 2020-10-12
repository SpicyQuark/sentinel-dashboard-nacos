package com.alibaba.csp.sentinel.dashboard.rule.nacos.cluster;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.request.ClusterAppAssignMap;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;

@Component("clusterAssignConfigPublisher")
public class ClusterAssignConfigPublisher
    implements DynamicRulePublisher<List<ClusterAppAssignMap>> {
  @Autowired protected ConfigService configService;

  @Override
  public void publish(String app, List<ClusterAppAssignMap> rules) throws Exception {
    AssertUtil.notEmpty(app, "app name cannot be empty");
    if (rules == null) {
      return;
    }
    configService.publishConfig(
        app + getDataIdPostfix(), NacosConfigUtil.GROUP_ID, JSON.toJSONString(rules));
  }

  public String getDataIdPostfix() {
    return NacosConfigUtil.CLUSTER_MAP_DATA_ID_POSTFIX;
  }
}
