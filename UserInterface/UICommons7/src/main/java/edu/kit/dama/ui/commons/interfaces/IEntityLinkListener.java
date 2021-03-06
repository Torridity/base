/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.ui.commons.interfaces;

/**
 *
 * @author jejkal
 */
public interface IEntityLinkListener {

  public enum ENTITY_TYPE {

    STUDY, INVESTIGATION, DIGITAL_OBJECT, INGEST, DOWNLOAD;
  }

  public void fireEntityLinkClickedEvent(ENTITY_TYPE pType);

  public void fireEntityLinkClickedEvent(ENTITY_TYPE pType, Long pId);
}
