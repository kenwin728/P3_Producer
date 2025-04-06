# How To Run The Program
1. **Run P3_Consumer**
   - Make sure to run P3_Consumer on another machine/virtual machine first.
2. **Add Video Folders**
   - Create video folders with videos inside (Each folder will be assigned to one producer thread)
3. **Run P3_Producer**
   - Run `ProducerApp.java`, then input the configuration variables: p (number of producer threads), consumer host, consumer port, and video folder paths.
   - Find the consumer host by opening the cmd in the machine/virtual machine running P3_Consumer then enter:
     ```cmd
     ipconfig
     ```
     (Look for `IPv4 Address` under active connection. E.g. `192.168.3.128`)
   - Use the same consumer port you entered after running P3_Consumer. (E.g. `8080`)
   - Enter the video folder paths relative to the root directory of your project. (E.g. `video_folders/folder1`)
4. **Stop The Program**
   - Program will automatically stop after uploading all the videos.
   
