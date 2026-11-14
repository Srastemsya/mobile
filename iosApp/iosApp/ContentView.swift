import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    let roomLink: String?
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController(roomLink: roomLink)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    @State private var roomLink: String?
    var body: some View {
        ComposeView(roomLink: roomLink)
            .id(roomLink)
            .ignoresSafeArea()
            .onOpenURL { url in roomLink = url.absoluteString }
    }
}
