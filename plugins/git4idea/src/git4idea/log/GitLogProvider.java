/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package git4idea.log;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.vcs.log.*;
import git4idea.GitVcsCommit;
import git4idea.history.GitHistoryUtils;
import git4idea.history.browser.GitCommit;
import org.hanuna.gitalk.common.MyTimer;
import org.hanuna.gitalk.git.reader.RefReader;
import org.hanuna.gitalk.log.commit.parents.SimpleCommitParents;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author Kirill Likhodedov
 */
public class GitLogProvider implements VcsLogProvider {

  private final Project myProject;

  public GitLogProvider(@NotNull Project project) {
    myProject = project;
  }

  @NotNull
  @Override
  public List<CommitParents> readNextBlock(@NotNull VirtualFile root, @NotNull Consumer<String> statusUpdater) throws VcsException {
    // TODO either don't query details here, or save them right away
    List<GitCommit> history = GitHistoryUtils.history(myProject, root);
    return ContainerUtil.map(history, new Function<GitCommit, CommitParents>() {
      @Override
      public CommitParents fun(GitCommit gitCommit) {
        return new SimpleCommitParents(Hash.build(gitCommit.getHash().getValue()),
                                       ContainerUtil.map(gitCommit.getParentsHashes(), new Function<String, Hash>() {
                                         @Override
                                         public Hash fun(String s) {
                                           return Hash.build(s);
                                         }
                                       }));
      }
    });
  }

  @NotNull
  @Override
  public List<CommitData> readCommitsData(@NotNull VirtualFile root, @NotNull List<String> hashes) throws VcsException {
    List<GitCommit> gitCommits;
    MyTimer timer = new MyTimer();
    timer.clear();
    gitCommits = GitHistoryUtils.commitsDetails(myProject, new FilePathImpl(root), null, hashes);
    System.out.println("Details loading took " + timer.get() + "ms for " + hashes.size() + " hashes");

    List<CommitData> result = new SmartList<CommitData>();
    for (GitCommit gitCommit : gitCommits) {
      VcsCommit commit = new GitVcsCommit(gitCommit);
      result.add(new CommitData(commit, Hash.build(commit.getHash())));
    }
    return result;
  }

  @Override
  public Collection<? extends Ref> readAllRefs(@NotNull VirtualFile root) throws VcsException {
    return new RefReader(myProject, false).readAllRefs(root);
  }
}
