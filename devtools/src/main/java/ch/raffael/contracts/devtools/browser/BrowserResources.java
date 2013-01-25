/*
 * Copyright 2012-2013 Raffael Herzog
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
package ch.raffael.contracts.devtools.browser;

import javax.swing.ImageIcon;

import ch.raffael.util.i18n.Default;
import ch.raffael.util.i18n.ResourceBundle;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface BrowserResources extends ResourceBundle {

    @Default("Rescan Folders")
    String rescan();

    @Default("refresh.png")
    ImageIcon rescanIcon();

    @Default("Add Folder")
    String addFolder();

    @Default("folder_add.png")
    ImageIcon addFolderIcon();

    @Default("Remove Folder")
    String removeFolder();

    @Default("folder_delete.png")
    ImageIcon removeFolderIcon();

    @Default("Reload")
    String reloadClass();

    @Default("refresh.png")
    ImageIcon reloadClassIcon();

    @Default("Forward")
    String forward();

    @Default("forward.png")
    ImageIcon forwardIcon();

    @Default("Back")
    String back();

    @Default("back.png")
    ImageIcon backIcon();

    @Default("folder.png")
    ImageIcon treeFolderIcon();

    @Default("package.png")
    ImageIcon treePackageIcon();

    @Default("class.png")
    ImageIcon treeClassIcon();

}
