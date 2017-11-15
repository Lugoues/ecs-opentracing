# Dockerfile
FROM postgres:9.4
ENV POSTGRES_DB peopledb 

RUN mkdir -p /tmp/psql_data/
COPY people.csv /tmp/psql_data
COPY import_people.sql /docker-entrypoint-initdb.d/
