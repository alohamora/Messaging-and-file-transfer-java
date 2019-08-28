# Messaging-and-file-transfer-java
A client and server application to upload files to server, create groups and share messages/files in groups

## Client CLI options
The client is initialised using `java Client $USER_NAME`
### Group related options
- create_group `GROUP_NAME`
- join_group `GROUP_NAME`
- leave_group `GROUP_NAME`
- share_msg `GROUP_NAME`

### Server details related options
- list_details `GROUP_NAME`
- list_groups

### File/folder related options
- upload `ABS_FILE_PATH`
- upload_udp `ABS_FILE_PATH`
- create_folder `FOLDER_NAME`
- move_file `SOURCE_PATH` `DEST_PATH`
- get_file `FILE_PATH`, where `FILE_PATH` is of format => `GROUP_NAME/USER_NAME/FILE_NAME`