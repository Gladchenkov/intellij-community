package org.hanuna.gitalk.git.reader.util;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author erokhins
 */
public class GitProcessFactory {
  private static final String ALL = "HEAD --branches --remotes --tags"; // no index, no stashes
  private final static String DEFAULT_LOG_REQUEST = "git log " + ALL + " --date-order --sparse --encoding=UTF-8 --full-history";
  private final static String TIMESTAMP_COMMIT_PARENTS_LOG_FORMAT = "--format=%ct|-%H|-%p";

  public static GitProcessFactory getInstance() {
    return new GitProcessFactory();
  }

  public Process firstPart(VirtualFile root, int maxCount) throws IOException {
    String request = DEFAULT_LOG_REQUEST + " --max-count=" + maxCount + " " + TIMESTAMP_COMMIT_PARENTS_LOG_FORMAT;
    return request(root, request);
  }

  public Process logPart(VirtualFile root, long startTimestamp, int maxCount) throws IOException {
    String request = "git log --before=" + startTimestamp + " " + ALL + " --max-count=" + maxCount + " --date-order" +
                     " --pretty=format:%ct|-%H|-%p --encoding=UTF-8 --full-history --sparse";
    return request(root, request);
  }

  public Process refs(VirtualFile root) throws IOException {
    String request = "git log " + ALL + " --no-walk --format=%H%d --decorate=full ";
    return request(root, request);
  }

  private static Process request(@NotNull VirtualFile root, String request) throws IOException {
    System.out.println(request);
    return Runtime.getRuntime().exec(request, ArrayUtil.EMPTY_STRING_ARRAY, new File(root.getPath()));
  }

}
