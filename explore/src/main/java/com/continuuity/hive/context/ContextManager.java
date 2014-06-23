package com.continuuity.hive.context;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.guice.ConfigModule;
import com.continuuity.common.guice.DiscoveryRuntimeModule;
import com.continuuity.common.guice.LocationRuntimeModule;
import com.continuuity.common.guice.ZKClientModule;
import com.continuuity.data.runtime.DataFabricModules;
import com.continuuity.data2.dataset2.DatasetFramework;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.twill.zookeeper.ZKClientService;

import java.io.IOException;

/**
 * Stores/creates context for Hive queries to run in MapReduce jobs.
 */
public class ContextManager {
  private static DatasetFramework datasetFramework;

  public static void initialize(DatasetFramework datasetFramework) {
    ContextManager.datasetFramework = datasetFramework;
  }

  public static DatasetFramework getDatasetManager(Configuration conf) throws IOException {
    if (datasetFramework == null) {
      selfInit(conf);
    }

    return datasetFramework;
  }

  private static void selfInit(Configuration conf) throws IOException {
    // Self init needs to happen only when running in as a MapReduce job.
    // In other cases, ContextManager will be initialized using initialize method.

    CConfiguration cConf = ConfigurationUtil.get(conf, Constants.Explore.CCONF_KEY, CConfCodec.INSTANCE);
    Configuration hConf = ConfigurationUtil.get(conf, Constants.Explore.HCONF_KEY, HConfCodec.INSTANCE);

    Injector injector = Guice.createInjector(
      new ConfigModule(cConf, hConf),
      new ZKClientModule(),
      new LocationRuntimeModule().getDistributedModules(),
      new DiscoveryRuntimeModule().getDistributedModules(),
      new DataFabricModules().getDistributedModules()
    );

    ZKClientService zkClientService = injector.getInstance(ZKClientService.class);
    // TODO: need to stop zkClientService at the end - REACTOR-273
    zkClientService.startAndWait();

    datasetFramework = injector.getInstance(DatasetFramework.class);
  }
}
