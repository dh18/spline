/*
 * Copyright 2017 Barclays Africa Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.spline.persistence.atlas

import java.io.{File, IOException}

import org.apache.commons.configuration.Configuration
import za.co.absa.spline.persistence.api.{DataLineagePersistor, ExecutionPersistor, PersistenceFactory}


/**
  * The object contains static information about settings needed for initialization of the AtlasPersistenceFactory class.
  */
object AtlasPersistenceFactory{
  val atlasPropertyPrefix = "atlas"
  val atlasConfigurationDirKey = "atlas.conf"
  val atlasTemporaryConfigurationFileName = "atlas-application.properties"
}

/**
  * The class represents a factory creating Atlas persistence layers for all main data lineage entities.
  *
  * @param configuration A source of settings
  */
class AtlasPersistenceFactory(configuration: Configuration) extends PersistenceFactory(configuration){

  import AtlasPersistenceFactory._
  import scala.collection.JavaConverters._

  createAtlasTemporaryConfigurationFile()

  def createTempDirectory() : File =
  {
    val temp = File.createTempFile("temp", System.nanoTime().toString)
    if(!temp.delete) throw new IOException("Could not delete temp file: " + temp.getAbsolutePath)
    if(!temp.mkdir) throw new IOException("Could not create temp directory: " + temp.getAbsolutePath)
    temp
  }

  private def createAtlasTemporaryConfigurationFile() : Unit = {
    val tempDir = createTempDirectory()
    val tempFile = new File(tempDir,atlasTemporaryConfigurationFileName)
    System.setProperty(atlasConfigurationDirKey, tempDir.getAbsolutePath)
    tempFile.deleteOnExit()
    tempDir.deleteOnExit()
    val printStream  = new java.io.PrintStream(tempFile)
    configuration.getKeys(atlasPropertyPrefix).asScala.foreach(
      key => {
        val value = configuration.getProperty(key)
        printStream.println(s"$key=$value")
      }
    )
    printStream.flush()
    printStream.close()
  }

  /**
    * The method creates a persistence layer for the [[za.co.absa.spline.model.DataLineage DataLineage]] entity.
    *
    * @return A persistence layer for the [[za.co.absa.spline.model.DataLineage DataLineage]] entity
    */
  override def createDataLineagePersistor(): DataLineagePersistor = new AtlasDataLineagePersistor

  /**
    * The method creates a persistence layer for the [[za.co.absa.spline.model.Execution Execution]] entity.
    *
    * @return A persistence layer for the [[za.co.absa.spline.model.Execution Execution]] entity
    */
  override def createExecutionPersistor(): ExecutionPersistor = new NopAtlasExecutionPersistor
}
