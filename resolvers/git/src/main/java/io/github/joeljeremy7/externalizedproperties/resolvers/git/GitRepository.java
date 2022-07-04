package io.github.joeljeremy7.externalizedproperties.resolvers.git;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * A Git repository representation.
 */
public class GitRepository {
    private final String uri;
    private final String branch;
    private final Path cloneDirectory;
    private final CredentialsProvider credentialsProvider;
    private final SshSessionFactory sshSessionFactory;

    /**
     * Constructor.
     * 
     * @param uri The Git repository URI.
     * @param branch The Git branch.
     * @param cloneDirectory The Git clone directory.
     * @param credentialsProvider The Git credentials provider.
     * @param sshSessionFactory The Git SSH session factory.
     */
    private GitRepository(
            String uri,
            String branch,
            Path cloneDirectory,
            CredentialsProvider credentialsProvider,
            SshSessionFactory sshSessionFactory
    ) {
        this.uri = uri;
        this.branch = branch;
        this.cloneDirectory = cloneDirectory;
        this.credentialsProvider = credentialsProvider;
        this.sshSessionFactory = sshSessionFactory;
    }

    /**
     * Checkout a file from the Git repository. The path will be resolved relative 
     * to the root directory of the Git working tree. 
     * 
     * @implNote If the Git repository has not yet been cloned locally, this will 
     * attempt to clone the remote Git repository to a local directory as specified 
     * in {@link Builder#cloneDirectory(Path)}.
     * 
     * @param pathToCheckout The path to the file to checkout. The path will be 
     * resolved relative to the root directory of the Git working tree.
     * @return This builder.
     */
    public Path checkout(String pathToCheckout) {
        if (pathToCheckout == null || pathToCheckout.isBlank()) {
            throw new IllegalArgumentException(
                "pathToCheckout must not be null or blank."
            );
        }

        try (Git git = gitCloneOrOpenRepo()) {
            return gitCheckoutPath(git, branch, pathToCheckout);
        }
    }

    private Git gitCloneOrOpenRepo() {
        try {
            if (Files.exists(cloneDirectory.resolve(".git"))) {
                // Open existing git repo.
                return Git.open(cloneDirectory.toFile());
            } else {
                cleanDirectory(cloneDirectory);
                return gitCloneRepo();
            }
        } catch (GitAPIException | IOException e) {
            throw new ExternalizedPropertiesException(
                "An exception occurred while attempting to clone/open Git repository.",
                e
            );
        }
    }

    private Git gitCloneRepo() throws GitAPIException {
        // Clone repo but don't checkout anything yet.
        CloneCommand clone = Git.cloneRepository()
            .setURI(uri)
            .setDirectory(cloneDirectory.toFile())
            .setNoCheckout(true)
            .setCloneAllBranches(false);

        if (credentialsProvider != null) {
            clone.setCredentialsProvider(credentialsProvider);
        } 
        
        if (sshSessionFactory != null) {
            clone.setTransportConfigCallback(transport -> {
                if (transport instanceof SshTransport) {
                    ((SshTransport)transport).setSshSessionFactory(sshSessionFactory);
                }
            });
        }

        return clone.call();
    }

    private static Path gitCheckoutPath(
            Git git, 
            String branchToCheckout,
            String pathToCheckout
    ) {
        boolean branchHasRemote = branchToCheckout.indexOf('/') != -1;
        
        try {
            // Checkout specific file.
            git.checkout()
                // Default remote name is origin.
                .setStartPoint(branchHasRemote ? 
                    branchToCheckout : "origin/" + branchToCheckout)
                .addPath(pathToCheckout)
                .call();
        } catch (GitAPIException e) {
            throw new ExternalizedPropertiesException(
                "An exception occurred while checking out resource file.",
                e
            );
        }
        
        return git.getRepository().getWorkTree().toPath().resolve(pathToCheckout);
    }

    private static void cleanDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (Stream<Path> paths = Files.walk(dir)) {
                // Delete from bottom up.
                for (Path path : paths.sorted(Comparator.reverseOrder())
                        .toArray(Path[]::new)) {
                    Files.delete(path);
                }
            }
        }

        Files.createDirectories(dir);
    }

    /**
     * Create a new {@link Builder} to facilitate building a Git repository 
     * represented by a {@link GitRepository} instance. 
     * 
     * @return The builder for {@link GitRepository}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The  {@link GitResolver}.
     */
    public static class Builder {
        private String uri;
        private String branch;
        private Path cloneDirectory;
        private CredentialsProvider credentialsProvider;
        private SshSessionFactory sshSessionFactory;

        @SuppressWarnings("NullAway.Init")
        private Builder() {}

        /**
         * The Git repository URI.
         * 
         * @param uri The Git repository URI.
         * @return This builder.
         */
        public Builder uri(String uri) {
            if (uri == null || uri.isBlank()) {
                throw new IllegalArgumentException("uri must not be null or blank.");
            }
            this.uri = uri;
            return this;
        }
        
        /**
         * The Git branch to work. The argument can either have a remote i.e. 
         * {@code origin/main} or just the branch name i.e. {@code main} in 
         * which case the remote will be assumed to be {@code origin}.
         * 
         * @param branch The Git branch to work with. The argument can either 
         * have a remote i.e. {@code origin/main} or just the branch name i.e. 
         * {@code main} in which case the remote will be assumed to be {@code origin}.
         * @return This builder.
         */
        public Builder branch(String branch) {
            if (branch == null || branch.isBlank()) {
                throw new IllegalArgumentException("branch must not be null or blank.");
            }
            this.branch = branch;
            return this;
        }

        /**
         * The directory where the Git repository will be cloned to. 
         * 
         * @implNote If the directory does not exist, it will automatically be created. 
         * Otherwise, the contents of the existing directory will be cleaned/deleted
         * before attempting to clone the Git repository.
         * 
         * @param cloneDirectory The directory where the Git repository will be cloned to.
         * @return This builder.
         */
        public Builder cloneDirectory(Path cloneDirectory) {
            if (cloneDirectory == null) {
                throw new IllegalArgumentException("cloneDirectory must not be null.");
            }
            this.cloneDirectory = cloneDirectory;
            return this;
        }

        /**
         * The Git credentials provider. This will only be used when connecting to a Git 
         * repository via HTTP(s).
         * 
         * @param credentialsProvider The Git credentials provider. This will only be used 
         * when connecting to a Git repository via HTTP(s).
         * @return This builder.
         */
        public Builder credentialsProvider(CredentialsProvider credentialsProvider) {
            if (credentialsProvider == null) {
                throw new IllegalArgumentException(
                    "credentialsProvider must not be null."
                );
            }
            this.credentialsProvider = credentialsProvider;
            return this;
        }

        /**
         * The Git SSH session factory. This will only be used when connecting to a Git 
         * repository via SSH.
         * 
         * @param sshSessionFactory The Git SSH session factory. This will only be used when 
         * connecting to a Git repository via SSH.
         * @return This builder.
         */
        public Builder sshSessionFactory(SshSessionFactory sshSessionFactory) {
            if (sshSessionFactory == null) {
                throw new IllegalArgumentException(
                    "sshSessionFactory must not be null."
                );
            }
            this.sshSessionFactory = sshSessionFactory;
            return this;
        }

        /**
         * Build the {@link GitResolver} by initializing the Git repository and reading
         * the target configuration resource.
         * 
         * @return The built {@link GitResolver}.
         */
        public GitRepository build() {
            validate();

            return new GitRepository(
                uri,
                branch,
                cloneDirectory,
                credentialsProvider,
                sshSessionFactory
            );
        }

        private void validate() {
            if (uri == null) {
                throw new IllegalStateException("Git repository URI is required.");
            }

            if (branch == null) {
                throw new IllegalStateException("Git branch is required.");
            }

            if (cloneDirectory == null) {
                throw new IllegalStateException("Git clone directory path is required.");
            }
        }
    }
}
