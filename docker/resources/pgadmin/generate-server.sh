#!/bin/sh

cat <<EOF > /pgadmin4/servers.json
{
  "Servers": {
    "1": {
      "Name": "${USER_SERVER_NAME}",
      "Group": "Servers",
      "Host": "${USER_SERVER_HOST}",
      "Port": ${USER_DATABASE_PORT},
      "MaintenanceDB": "${USER_DATABASE_NAME}",
      "Username": "${USER_DATABASE_USER}",
      "Password": "${USER_DATABASE_PASSWORD}",
      "SSLMode": "prefer"
    },
    "2": {
      "Name": "${NOTIFICATION_SERVER_NAME}",
      "Group": "Servers",
      "Host": "${NOTIFICATION_SERVER_HOST}",
      "Port": ${NOTIFICATION_DATABASE_PORT},
      "MaintenanceDB": "${NOTIFICATION_DATABASE_NAME}",
      "Username": "${NOTIFICATION_DATABASE_USER}",
      "Password": "${NOTIFICATION_DATABASE_PASSWORD}",
      "SSLMode": "prefer"
    },
    "3": {
      "Name": "${THREAD_SERVER_NAME}",
      "Group": "Servers",
      "Host": "${THREAD_SERVER_HOST}",
      "Port": ${THREAD_DATABASE_PORT},
      "MaintenanceDB": "${THREAD_DATABASE_NAME}",
      "Username": "${THREAD_DATABASE_USER}",
      "Password": "${THREAD_DATABASE_PASSWORD}",
      "SSLMode": "prefer"
    }
  }
}
EOF

exec /entrypoint.sh
