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
    },
    "4": {
      "Name": "${POST_SERVER_NAME}",
      "Group": "Servers",
      "Host": "${POST_SERVER_HOST}",
      "Port": ${POST_DATABASE_PORT},
      "MaintenanceDB": "${POST_DATABASE_NAME}",
      "Username": "${POST_DATABASE_USER}",
      "Password": "${POST_DATABASE_PASSWORD}",
      "SSLMode": "prefer"
    },
    "5": {
      "Name": "${CATEGORY_SERVER_NAME}",
      "Group": "Servers",
      "Host": "${CATEGORY_SERVER_HOST}",
      "Port": ${CATEGORY_DATABASE_PORT},
      "MaintenanceDB": "${CATEGORY_DATABASE_NAME}",
      "Username": "${CATEGORY_DATABASE_USER}",
      "Password": "${CATEGORY_DATABASE_PASSWORD}",
      "SSLMode": "prefer"
    },
    "6": {
      "Name": "${MESSAGE_SERVER_NAME}",
      "Group": "Servers",
      "Host": "${MESSAGE_SERVER_HOST}",
      "Port": ${MESSAGE_DATABASE_PORT},
      "MaintenanceDB": "${MESSAGE_DATABASE_NAME}",
      "Username": "${MESSAGE_DATABASE_USER}",
      "Password": "${MESSAGE_DATABASE_PASSWORD}",
      "SSLMode": "prefer"
    },
    "7": {
      "Name": "${REACTION_SERVER_NAME}",
      "Group": "Servers",
      "Host": "${REACTION_SERVER_HOST}",
      "Port": ${REACTION_DATABASE_PORT},
      "MaintenanceDB": "${REACTION_DATABASE_NAME}",
      "Username": "${REACTION_DATABASE_USER}",
      "Password": "${REACTION_DATABASE_PASSWORD}",
      "SSLMode": "prefer"
    }
  }
}
EOF

exec /entrypoint.sh
