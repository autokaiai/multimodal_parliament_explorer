FROM "kjarosh/latex:2024.4-small"

WORKDIR /app
COPY pom.xml /app

# JDK 17, maven, .. runterladen
RUN apk add --no-cache \
    bash \
    openjdk17 \
    maven \
    git

RUN mvn dependency:resolve-plugins dependency:resolve -B

COPY . /app

# tex-packages
RUN tlmgr install \
    latexmk \
    relsize \
    frankenstein \
    geometry \
    pgfplots \
    babel-german \
    graphics \
    hyperref \
    xcolor \
    pgf-pie \
    luacode \
    luaimageembed \
    pgf

# mehr heap space und größerer buffer für große pdfs
ENV JAVA_OPTS="-Xmx80%"
RUN echo "buf_size=1000000" >> /opt/texlive/texmf.cnf

RUN mvn clean compile -B

RUN chmod +x /app/entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/app/entrypoint.sh"]
