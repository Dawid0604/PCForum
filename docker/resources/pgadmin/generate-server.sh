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
    }
  }
}
EOF

exec /entrypoint.sh
