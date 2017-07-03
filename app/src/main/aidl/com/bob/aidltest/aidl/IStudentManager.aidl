// IStudentManager.aidl
package com.bob.aidltest.aidl;

import com.bob.aidltest.aidl.Student;

interface IStudentManager {
    List<Student> getStudentList();
    void addStudent(in Student student);
}
