////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2013 Denim Group, Ltd.
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.plugins.intellij.markers;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: mac
 * Date: 12/4/13
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkspaceUtils {

    private WorkspaceUtils(){}

    public static void openFile(Project project, VirtualFile file, int lineNumber) {

        if (file.isValid()) {
            FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, file, lineNumber), true);
        } else {
            System.out.println("VirtualFile.isValid() returned false. Possibly trying to open a deleted file.");
        }
    }

    public static Map<String, Set<VirtualFile>> getAllFilesAsMap(Project project) {
        Map<String, Set<VirtualFile>> map = new HashMap<String, Set<VirtualFile>>();

        if (project != null && project.getProjectFile() != null && project.getBaseDir().exists()) {

            MapEntryCollector entryCollector = new MapEntryCollector();
            visitChildrenRecursively(project.getBaseDir(), entryCollector);
            map = entryCollector.fileMap;
        }

        return map;
    }

    public static Collection<VirtualFile> getAllFilesAsCollection(Project project) {
        Collection<VirtualFile> map = new ArrayList<VirtualFile>();

        if (project != null && project.getProjectFile() != null && project.getBaseDir().exists()) {

            CollectionCollector entryCollector = new CollectionCollector();
            visitChildrenRecursively(project.getBaseDir(), entryCollector);
            map = entryCollector.fileCollection;
        }

        return map;
    }

    private static void visitChildrenRecursively(@NotNull VirtualFile file, Collector collector) {

        // IntelliJ warns about file.getChildren because of symlinks, but we check for symlinks.
        // This is the same protection afforded by their proposed VfsUtilCore solution.
        if (file.isDirectory() && !file.getName().startsWith(".") &&
                !file.is(VFileProperty.SYMLINK) && file.getChildren() != null) {
            for (VirtualFile childFile : file.getChildren()) {
                visitChildrenRecursively(childFile, collector);
            }
        } else if (!file.isDirectory()) {
            collector.collect(file);
        }
    }

    interface Collector {
        void collect(@NotNull VirtualFile file);
    }

    private static class MapEntryCollector implements Collector {

        private final Map<String, Set<VirtualFile>> fileMap = new HashMap<String, Set<VirtualFile>>();

        public void collect(@NotNull VirtualFile file) {
            if (!fileMap.containsKey(file.getName())) {
                fileMap.put(file.getName(), new HashSet<VirtualFile>());
            }

            fileMap.get(file.getName()).add(file);
        }
    }

    private static class CollectionCollector implements Collector {
        private final Collection<VirtualFile> fileCollection = new ArrayList<VirtualFile>();

        public void collect(@NotNull VirtualFile file) {
            fileCollection.add(file);
        }
    }

}