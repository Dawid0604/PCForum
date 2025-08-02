FROM dpage/pgadmin4:9.6.0
USER root

COPY generate-server.sh /generate-server-json.sh
RUN chmod +x /generate-server-json.sh

ENTRYPOINT [ "/generate-server-json.sh" ]