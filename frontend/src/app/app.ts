import { Component, signal, OnInit } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { Header } from './component/Header/header';
import { Footer } from './component/footer/footer';
import { filter } from 'rxjs/operators';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-root',
  imports: [RouterOutlet,Header,Footer, CommonModule],
  
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('ecommerce-app');
  shouldHideNavbar = false;

  constructor(private router: Router) {}

  ngOnInit() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      const hiddenRoutes = ['/login', '/register', '/product-listing', '/admin'];
      this.shouldHideNavbar = hiddenRoutes.some(route => event.url.startsWith(route));
      
      // Update body class for styling
      if (this.shouldHideNavbar) {
        document.body.classList.add('no-header-page');
      } else {
        document.body.classList.remove('no-header-page');
      }
    });
  }
}
