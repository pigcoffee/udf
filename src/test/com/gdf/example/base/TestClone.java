package com.gdf.example.base;

class Test {
    public static int X = 100;
    public final static int Y = 200;

    public Test() {
        System.out.println("Test构造函数执行");
    }

    static {
        System.out.println("static语句块执行");
    }

    public static void display() {
        System.out.println("静态方法被执行");
    }

    public void display_1() {
        System.out.println("实例方法被执行");
    }
}

class User {
	String name;
	int age;
	@Override
	public String toString() {
		return "User [name=" + name + ", age=" + age + "]";
	}
}

class Account implements Cloneable {
	User user;
	long balance;
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}

class DeepClone implements Cloneable {
	User user;
	long balance;
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}


public class TestClone {

	public static void main(String[] args) throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		Class clazz = Test.class;
//		System.out.println(clazz);
//		try {
//			Class.forName("com.gdf.example.Test");
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
		
		User user = new User();
		user.name = "aaaaaa";
		user.age = 10;
		
		Account account = new Account();
		account.user = user;
		account.balance = 100;
		
		Account copy = (Account)account.clone();
		
		System.out.println(account.user.toString());
		System.out.println(copy.user.toString());
		
		
		System.out.println("=============================");
		copy.user.name = "newName";
		System.out.println(account.user.toString());
		System.out.println(copy.user.toString());

	}

}


