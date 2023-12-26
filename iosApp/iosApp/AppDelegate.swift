//
//  AppDelegate.swift
//  iosApp
//
//  Created by Mikołaj on 26/12/2023.
//  Copyright © 2023 orgName. All rights reserved.
//
import UIKit
import ComposeApp
import Firebase

class AppDelegate: NSObject, UIApplicationDelegate {

  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {

    FirebaseApp.configure()

    return true
  }
}
