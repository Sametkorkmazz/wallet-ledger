import ThemeToggle from "@/components/ThemeToggle";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import useAuth from "@/hooks/useAuth";
import { History, LayoutGrid, LogOut, Send, Wallet } from "lucide-react";
import {
  Link,
  NavLink,
  Outlet,
  type NavLinkRenderProps,
} from "react-router-dom";

const navClass: (props: NavLinkRenderProps) => string = ({ isActive }) =>
  "inline-flex items-center gap-2 rounded-md px-3 py-1.5 whitespace-nowrap " +
  (isActive
    ? "bg-primary/12 text-foreground font-medium"
    : "text-muted-foreground hover:text-foreground hover:bg-muted");

const container = "mx-auto w-full max-w-7xl px-6";

export default function AppLayout() {
  const { email } = useAuth();

  return (
    <div className="min-h-screen bg-muted/40">
      <header className="border-b bg-background">
        <div className={`${container} flex h-16 items-center gap-6`}>
          <Link
            to="/dashboard"
            className="flex items-center gap-2 font-semibold whitespace-nowrap"
          >
            <span className="flex size-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
              <Wallet className="size-5" />
            </span>
            <span className="sr-only sm:not-sr-only">Wallet &amp; Ledger</span>
          </Link>

          <nav aria-label="Main" className="flex gap-1 text-sm">
            <NavLink className={navClass} to="/dashboard">
              <LayoutGrid className="size-4" />
              <span className="sr-only lg:not-sr-only">Dashboard</span>
            </NavLink>
            <NavLink className={navClass} to="/transfer">
              <Send className="size-4" />
              <span className="sr-only lg:not-sr-only">Send money</span>
            </NavLink>
            <NavLink className={navClass} to="/transactions">
              <History className="size-4" />
              <span className="sr-only lg:not-sr-only">History</span>
            </NavLink>
          </nav>
          <div className="ml-auto flex items-center gap-3">
            <Avatar className="size-8">
              <AvatarFallback className="text-xs">
                {email.slice(0, 2).toUpperCase()}
              </AvatarFallback>
            </Avatar>
            <span className="hidden text-sm xl:inline">{email}</span>
            <Button variant="ghost" size="sm">
              <LogOut />
              <span className="sr-only lg:not-sr-only">Sign out</span>
            </Button>
            <ThemeToggle />
          </div>
        </div>
      </header>
      <main className={`${container} py-8`}>
        <Outlet />
      </main>
    </div>
  );
}
