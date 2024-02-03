# # See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.205.2/containers/java
ARG VARIANT="21"
FROM mcr.microsoft.com/vscode/devcontainers/java:${VARIANT}

# # ** [Optional] Uncomment this section to install additional packages. **
# # RUN apk update \
# #     && apk add --no-cache <your-package-list-here>

# Install Clojure - see https://github.com/clojure/brew-install/releases/
#   and https://clojure.org/guides/install_clojure#_linux_instructions
ENV CLOJURE_VERSION=1.11.1.1435
WORKDIR /tmp
RUN \
apt-get update && \
apt-get install -y curl make rlwrap wget && \
rm -rf /var/lib/apt/lists/* && \
wget https://github.com/clojure/brew-install/releases/download/$CLOJURE_VERSION/linux-install.sh && \
chmod +x linux-install.sh && \
./linux-install.sh && \
clojure -e "(clojure-version)" && \
apt-get purge -y --auto-remove curl wget && \
rm ./linux-install.sh

# Pre-cache dependencies
COPY deps.edn .
COPY package.json .
COPY yarn.lock .
RUN clojure -A:calva -Spath

EXPOSE 8000
EXPOSE 9631
EXPOSE 9001

#CMD ["clojure", "-A:nrepl", "--bind", "0.0.0.0"]

#########################################
# FROM clojure:tools-deps-alpine

# WORKDIR /app
# COPY deps.edn .
# RUN clojure -R:nrepl -Spath

# EXPOSE 8088
# EXPOSE 52162

# CMD ["clojure", "-A:nrepl", "--bind", "0.0.0.0"]
