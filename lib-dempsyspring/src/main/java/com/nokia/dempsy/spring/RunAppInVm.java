/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nokia.dempsy.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.nokia.dempsy.Dempsy;
import com.nokia.dempsy.DempsyException;

public class RunAppInVm
{
   protected static Logger logger = LoggerFactory.getLogger(RunAppInVm.class);
   protected static final String appdefParam = "appdef";
   protected static final String applicationParam = "application";
   
   public static void main(String[] args)
   {
      try
      {
         run(args);
      }
      catch(Throwable e)
      {
         e.printStackTrace(System.err);
         System.err.flush();
         System.exit(1);
      }
      System.exit(0);
   }
   public static void run(String[] args) throws Throwable
   {
      //======================================================
      // Handle all of the options.
      String appCtxFilename = System.getProperty(appdefParam);
      if (appCtxFilename == null || appCtxFilename.length() == 0)
      {
//         usage("the java vm option \"-D" + appdefParam + "\" wasn't specified.");
         String application = System.getProperty(applicationParam);
         if(application == null || application.length() == 0)
            usage("the java vm option \"-D" + appdefParam + "\" or the java vm option \"-D"+applicationParam + "\" wasn't specified.");
         appCtxFilename = "DempsyApplicationContext-"+application+".xml";
      }
      //======================================================
      
      String contextFile = "classpath:Dempsy-localVm.xml";
      ClassPathXmlApplicationContext context = null;
      try
      {
         // Initialize Spring
         context = new ClassPathXmlApplicationContext(new String[] {appCtxFilename, contextFile});
         context.registerShutdownHook();
      }
      catch(Throwable e)
      {
         logger.error(MarkerFactory.getMarker("FATAL"), "Failed to start the application ", e);
         throw e;
      }
      if(context != null)
      {
         try
         {
            context.getBean(Dempsy.class).waitToBeStopped();
         }
         catch(InterruptedException e)
         {
            logger.error("Interrupted . . . ", e);
         }
         finally
         {
            context.stop();
         }
         
         logger.info("Shut down dempsy appliction "+appCtxFilename + ", bye!");
      }
   }
   
   public static void usage(String errorMessage) throws DempsyException
   {
      StringBuilder sb = new StringBuilder();
      if (errorMessage != null)
         sb.append("ERROR:" + errorMessage + "\n");
      sb.append("usage example: java [-D"+ appdefParam + "=MyAppDefinition.xml|-D"+applicationParam+"=testApp ] -cp (classpath) " + RunAppInVm.class.getName() + "\n");
      sb.append("    -D" + appdefParam + " must be supplied to indicate the Dempsy application definition's spring application context xml file.\n");
      sb.append("           A file with the name given that contains a spring application context xml must be on the classpath.\n");
      sb.append("           OR ");
      sb.append("    -D" + applicationParam + " must be supplied to indicate the Dempsy application name defined in spring application context xml file.\n");
      sb.append("           A file with the name DempsyApplicationContext-${applicaitonName}.xml that contains a spring applicaiton context xml must be on the classpath.");
      
      logger.error(MarkerFactory.getMarker("FATAL"), sb.toString());
      throw new DempsyException(sb.toString());
   }

}
