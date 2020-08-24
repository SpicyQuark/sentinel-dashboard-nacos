package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RuleNacosProvider <T extends RuleEntity>  implements DynamicRuleProvider<List<T>> {
    @Autowired
    protected ConfigService configService;
    @Autowired
    protected Converter<String, List<T>> converter;
    private AtomicBoolean listenerInit = new AtomicBoolean(false);

    @Override
    public List<T> getRules(String appName) throws Exception {
        String rules = configService.getConfig(appName + getDataIdPostfix(),
                NacosConfigUtil.GROUP_ID, 3000);
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        if(listenerInit.compareAndSet(false, true))
        	initNacosCosoleListener(appName);
        return converter.convert(rules);
    }

    /**
     * 注册listener为了与nacos控制台的[配置管理-监听查询]交互
     * <p>由于本项目没有使用NacosDataSource机制进行构建,因此ConfigService默认没有注册Listener,
     * <br>即无法触发NacosConfigService.worker初始化CacheData通过LongPollingRunnable定时执行ClientWorker.checkUpdateConfigStr()
     * <br>请求nacos控制台"/v1/cs/configs/listener"进行监听显示
     * <p>当前只有在请求过规则页面才会运行到此处代码,若有优化方案可进行迁移
     * @param app
     * @throws Exception 
     */
    public void initNacosCosoleListener(String app) throws Exception {
    	try {
    		configService.addListener(app + getDataIdPostfix(), NacosConfigUtil.GROUP_ID, new Listener() {
				
				@Override
				public void receiveConfigInfo(String configInfo) {
					// do nothing
				}
				
				@Override
				public Executor getExecutor() {
					// TODO 返回空时使用当前线程进行逻辑,CacheData.safeNotifyListener()
					return null;
				}
			});
		} catch (Exception e) {
			listenerInit.set(false);
			throw e;
		}
    }
    
    /**
     * 文件后缀 参考 com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil.FLOW_DATA_ID_POSTFIX
     * @return
     */
    public abstract String getDataIdPostfix();
}
