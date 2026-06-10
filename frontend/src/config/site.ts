export const siteConfig = {
  name: "ERP Platform",
  description: "Enterprise Resource Planning system for SMEs",
  url: process.env.NEXT_PUBLIC_APP_URL || "http://localhost:3000",
  ogImage: "https://your-domain.com/og.jpg",
  links: {
    github: "https://github.com/your-repo",
  },
  mainNav: [
    {
      title: "Dashboard",
      href: "/dashboard",
    },
    {
      title: "Sales",
      href: "/sales",
    },
    {
      title: "Inventory",
      href: "/inventory",
    },
    {
      title: "Blockchain",
      href: "/blockchain",
    },
  ],
  sidebarNav: [
    {
      title: "Overview",
      href: "/",
      icon: "home",
    },
    {
      title: "Sales",
      href: "/sales",
      icon: "shopping-cart",
    },
    {
      title: "Inventory",
      href: "/inventory",
      icon: "package",
    },
    {
      title: "Blockchain",
      href: "/blockchain",
      icon: "link",
    },
  ],
}
