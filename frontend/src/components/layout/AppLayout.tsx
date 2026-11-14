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
  "inline-flex items-center gap-2 rounded-md px-3 py-1.5 " +
  (isActive
    ? "bg-primary/12 text-foreground font-medium"
    : "text-muted-foreground hover:text-foreground hover:bg-muted");

export default function AppLayout() {
  const { email } = useAuth();

  return (
    <div className="min-h-screen bg-muted/40">
      <header className="border-b bg-background">
        <div className="mx-auto flex h-14 items-center max-w-5xl gap-6 px-4">
          <Link
            to="/dashboard"
            className="flex items-center gap-2 font-semibold"
          >
            <span className="flex size-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
              <Wallet className="size-5" />
            </span>
            <span>Wallet &amp; Ledger</span>
          </Link>

          <nav aria-label="Main" className="flex gap-1 text-sm">
            <NavLink className={navClass} to="/dashboard">
              <LayoutGrid className="size-4" />
              Dashboard
            </NavLink>
            <NavLink className={navClass} to="/transfer">
              <Send className="size-4" />
              Send money
            </NavLink>
            <NavLink className={navClass} to="/transactions">
              <History className="size-4" />
              History
            </NavLink>
          </nav>
          <div className="flex items-center gap-3 ml-auto">
            <Avatar className="size-8">
              <AvatarFallback className="text-xs">
                {email.slice(0, 2).toUpperCase()}
              </AvatarFallback>
            </Avatar>
            <span className="text-sm hidden sm:inline">{email}</span>
            <Button variant="ghost" size="sm">
              <LogOut />
              Sign out
            </Button>
            <ThemeToggle />
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
}
